"use client";

import styles from "./AvailabilityToggle.module.css";

interface AvailabilityToggleProps {
  checked: boolean;
  disabled: boolean;
  ariaLabel: string;
  /** isAssigned인 슬롯은 제외 */
  onToggle?: () => void;
}

export function AvailabilityToggle({ checked, disabled, ariaLabel, onToggle }: AvailabilityToggleProps) {
  return (
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={ariaLabel}
      className={styles.toggle}
      data-checked={checked}
      disabled={disabled}
      onClick={disabled ? undefined : onToggle}
    >
      <span className={styles.knob} />
    </button>
  );
}