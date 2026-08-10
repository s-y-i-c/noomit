"use client";

import { useState } from "react";
import styles from "./SelectorPlaceholder.module.css";

interface ProductSelectorPlaceholderProps {
  initialValue?: string;
  onSelect: (product: { productId: string }) => void;
}

// TODO: ProductSelector(검색/선택 UI) 컴포넌트 구현 후 교체
export function ProductSelectorPlaceholder({ initialValue = "", onSelect }: ProductSelectorPlaceholderProps) {
  const [tempId, setTempId] = useState(initialValue);

  const handleChange = (value: string) => {
    setTempId(value);
    onSelect({ productId: value.trim() });
  };

  return (
    <div className={styles.placeholder}>
      <p className={styles.label}>제품 정보 컴포넌트 자리 (TODO: ProductSelector 연동)</p>
      <input
        type="text"
        value={tempId}
        onChange={(event) => handleChange(event.target.value)}
        placeholder="제품 ID (임시 입력, 실제 컴포넌트 연동 전까지 사용)"
      />
    </div>
  );
}