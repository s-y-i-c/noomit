"use client";

import { useEffect, useRef, useState } from "react";
import { MapPin, RefreshCw, Search } from "lucide-react";
import { useLazyFindCustomerByPhoneNumberQuery } from "../api/customersApi";
import { openDaumPostcode, preloadDaumPostcode } from "../lib/daumPostcode";
import type { Customer, CustomerInfoFormValue } from "../types/customer";
import styles from "./CustomerInfoForm.module.css";

function toFormValue(customer?: Customer | null, keepPhoneNumber = ""): CustomerInfoFormValue {
  return {
    phoneNumber: customer?.phoneNumber ?? keepPhoneNumber,
    name: customer?.name ?? "",
    zipCode: customer?.zipCode ?? "",
    address: customer?.address ?? "",
    detailAddress: customer?.detailAddress ?? "",
    memo: customer?.memo ?? "",
  };
}

interface CustomerInfoFormProps {
  /** 접수 담당자가 이미 알고 있는 고객으로 미리 채워서 시작하고 싶을 때. */
  initialCustomer?: Customer | null;
  /** 필드가 바뀔 때마다(조회로 채워졌을 때 포함) 최신 값을 올려보낸다. 제출은 이걸 받는 쪽이 한다. */
  onChange: (value: CustomerInfoFormValue) => void;
}

/**
 * 전화번호로 기존 고객을 조회해 채우거나, 없으면 신규로 입력받는 폼.
 * 제출 버튼과 POST /api/customers 호출은 이 컴포넌트를 쓰는 쪽(접수 화면)의 책임이다.
 */
export function CustomerInfoForm({ initialCustomer, onChange }: CustomerInfoFormProps) {
  const [value, setValue] = useState<CustomerInfoFormValue>(() => toFormValue(initialCustomer));
  const [lookupState, setLookupState] = useState<"idle" | "found" | "new">(initialCustomer ? "found" : "idle");
  const [lookupError, setLookupError] = useState<string | null>(null);
  const [findCustomer, { isFetching }] = useLazyFindCustomerByPhoneNumberQuery();
  const [isAddressScriptReady, setIsAddressScriptReady] = useState(false);
  const [addressSearchError, setAddressSearchError] = useState<string | null>(null);
  const detailAddressRef = useRef<HTMLInputElement>(null);

  // 클릭 시점엔 이미 로드가 끝나 있어야 팝업 차단에 안 걸린다 — 마운트 시점에 미리 불러온다.
  useEffect(() => {
    preloadDaumPostcode()
      .then(() => setIsAddressScriptReady(true))
      .catch(() => setAddressSearchError("주소 검색 스크립트를 불러오지 못했습니다."));
  }, []);

  // initialCustomer가 바뀌면(다른 고객으로 교체) 폼을 그 값으로 리셋한다.
  // 렌더링 중에 바로 보정 — effect로 하면 리렌더가 한 번 더 걸린다.
  const [syncedCustomer, setSyncedCustomer] = useState(initialCustomer);
  if (initialCustomer !== syncedCustomer) {
    setSyncedCustomer(initialCustomer);
    if (initialCustomer) {
      setValue(toFormValue(initialCustomer));
      setLookupState("found");
    }
  }

  useEffect(() => {
    onChange(value);
  }, [value, onChange]);

  const update = <K extends keyof CustomerInfoFormValue>(key: K, fieldValue: CustomerInfoFormValue[K]) => {
    setValue((current) => ({ ...current, [key]: fieldValue }));
  };

  const handleLookup = async () => {
    const phoneNumber = value.phoneNumber.trim();
    if (!phoneNumber) return;

    setLookupError(null);
    try {
      const found = await findCustomer(phoneNumber).unwrap();
      if (found) {
        setValue(toFormValue(found));
        setLookupState("found");
      } else {
        setValue(toFormValue(null, phoneNumber));
        setLookupState("new");
      }
    } catch (reason) {
      const message = typeof reason === "object" && reason !== null && "message" in reason
        ? String(reason.message)
        : "고객 조회에 실패했습니다.";
      setLookupError(message);
      setLookupState("idle");
    }
  };

  const handleAddressSearch = () => {
    setAddressSearchError(null);
    const opened = openDaumPostcode((result) => {
      setValue((current) => ({ ...current, zipCode: result.zonecode, address: result.roadAddress }));
      detailAddressRef.current?.focus();
    });
    if (!opened) {
      setAddressSearchError("주소 검색을 아직 준비 중이에요. 잠시 후 다시 눌러주세요.");
    }
  };

  return (
    <div className={styles.form}>
      <div className={styles.twoColRow}>
        <div className={styles.phoneRow}>
          <label className={styles.phoneLabel}>
            <span>전화번호</span>
            <input
              value={value.phoneNumber}
              onChange={(e) => update("phoneNumber", e.target.value.replace(/\D/g, "").slice(0, 11))}
              inputMode="numeric"
              maxLength={11}
              placeholder="01000000000"
            />
          </label>
          <button
            type="button"
            className={styles.lookupButton}
            onClick={handleLookup}
            disabled={isFetching || !value.phoneNumber.trim()}
          >
            {isFetching ? <RefreshCw className={styles.spinning} size={16} /> : <Search size={16} />}
            조회
          </button>
        </div>

        <label>
          <span>이름</span>
          <input value={value.name} onChange={(e) => update("name", e.target.value)} required />
        </label>
      </div>

      {lookupError ? <p className={styles.hint} data-tone="error">{lookupError}</p> : null}
      {!lookupError && lookupState === "found" ? (
        <p className={styles.hint} data-tone="found">기존 고객 정보를 불러왔습니다. 내용을 확인하고 필요한 부분만 고쳐주세요.</p>
      ) : null}
      {!lookupError && lookupState === "new" ? (
        <p className={styles.hint} data-tone="new">신규 고객입니다. 정보를 입력해주세요.</p>
      ) : null}

      <div className={styles.phoneRow}>
        <label className={styles.phoneLabel}>
          <span>우편번호</span>
          <input
            value={value.zipCode}
            readOnly
            placeholder="주소 검색으로 자동 입력됩니다"
            required
          />
        </label>
        <button
          type="button"
          className={styles.lookupButton}
          onClick={handleAddressSearch}
          disabled={!isAddressScriptReady}
        >
          {!isAddressScriptReady ? <RefreshCw className={styles.spinning} size={16} /> : <MapPin size={16} />}
          주소 검색
        </button>
      </div>
      {addressSearchError ? <p className={styles.hint} data-tone="error">{addressSearchError}</p> : null}

      <label>
        <span>주소</span>
        <input value={value.address} onChange={(e) => update("address", e.target.value)} required />
      </label>
      <label>
        <span>상세주소</span>
        <input
          ref={detailAddressRef}
          value={value.detailAddress}
          onChange={(e) => update("detailAddress", e.target.value)}
        />
      </label>
      <label>
        <span>메모</span>
        <textarea value={value.memo} onChange={(e) => update("memo", e.target.value)} rows={3} />
      </label>
    </div>
  );
}
