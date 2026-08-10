"use client";

import { ChevronLeft, ChevronRight, ChevronsLeft, ChevronsRight } from "lucide-react";
import styles from "./ServiceRequestList.module.css";

const PAGE_BLOCK_SIZE = 5;

interface PaginationProps {
  page: number;
  totalPages: number;
  isFetching: boolean;
  onChange: (page: number) => void;
}

export function Pagination({ page, totalPages, isFetching, onChange }: PaginationProps) {
  const currentBlock = Math.floor(page / PAGE_BLOCK_SIZE);
  const blockStartPage = currentBlock * PAGE_BLOCK_SIZE;
  const blockEndPage = Math.min(blockStartPage + PAGE_BLOCK_SIZE, Math.max(totalPages, 1));
  const pageNumbers = Array.from(
    { length: blockEndPage - blockStartPage },
    (_, i) => blockStartPage + i,
  );

  return (
    <div className={styles.pagination}>
      <button
        type="button"
        className={styles.pageStep}
        onClick={() => onChange(0)}
        disabled={page <= 0 || isFetching}
      >
        <ChevronsLeft size={16} />
      </button>
      <button
        type="button"
        className={styles.pageStep}
        onClick={() => onChange(page - 1)}
        disabled={page <= 0 || isFetching}
      >
        <ChevronLeft size={16} />
      </button>
      {pageNumbers.map((pageNumber) => (
        <button
          key={pageNumber}
          type="button"
          className={styles.pageNumber}
          data-active={pageNumber === page}
          onClick={() => onChange(pageNumber)}
          disabled={isFetching}
        >
          {pageNumber + 1}
        </button>
      ))}
      <button
        type="button"
        className={styles.pageStep}
        onClick={() => onChange(page + 1)}
        disabled={page + 1 >= totalPages || isFetching}
      >
        <ChevronRight size={16} />
      </button>
      <button
        type="button"
        className={styles.pageStep}
        onClick={() => onChange(totalPages - 1)}
        disabled={page + 1 >= totalPages || isFetching}
      >
        <ChevronsRight size={16} />
      </button>
    </div>
  );
}