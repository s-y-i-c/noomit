"use client";

import { useState } from "react";
import styles from "../SelectorPlaceholder.module.css";

interface CustomerSelectorPlaceholderProps {
  initialValue?: string;
  onSelect: (customer: { customerId: string }) => void;
}

// TODO: CustomerSelector(검색/선택 UI) 컴포넌트 구현 후 교체
export function CustomerSelectorPlaceholder({ initialValue = "", onSelect }: CustomerSelectorPlaceholderProps) {
  const [tempId, setTempId] = useState(initialValue);

  const handleChange = (value: string) => {
    setTempId(value);
    onSelect({ customerId: value.trim() });
  };

  return (
    <div className={styles.placeholder}>
      <p className={styles.label}>고객 정보 컴포넌트 자리 (TODO: CustomerSelector 연동)</p>
      <input
        type="text"
        value={tempId}
        onChange={(event) => handleChange(event.target.value)}
        placeholder="고객 ID (임시 입력, 실제 컴포넌트 연동 전까지 사용)"
      />
    </div>
  );
}