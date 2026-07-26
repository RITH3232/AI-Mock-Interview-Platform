import * as React from 'react';
import { motion } from 'framer-motion';
import { cn } from '../../utils/utils';

export interface ProgressBarProps {
  value: number; // 0-100
  className?: string;
  trackClassName?: string;
  barClassName?: string;
  label?: string;
  showValue?: boolean;
  colorFor?: (value: number) => string;
}

const defaultColorFor = (value: number) => {
  if (value >= 75) return 'bg-emerald-500';
  if (value >= 50) return 'bg-primary';
  if (value >= 25) return 'bg-amber-500';
  return 'bg-destructive';
};

export const ProgressBar: React.FC<ProgressBarProps> = ({
  value,
  className,
  trackClassName,
  barClassName,
  label,
  showValue = false,
  colorFor = defaultColorFor,
}) => {
  const clamped = Math.max(0, Math.min(100, value));

  return (
    <div className={cn('w-full', className)}>
      {(label || showValue) && (
        <div className="flex items-center justify-between mb-1.5 text-xs font-semibold text-muted-foreground">
          {label && <span>{label}</span>}
          {showValue && <span className="text-foreground">{Math.round(clamped)}</span>}
        </div>
      )}
      <div className={cn('h-2 w-full rounded-full bg-accent overflow-hidden', trackClassName)}>
        <motion.div
          className={cn('h-full rounded-full', colorFor(clamped), barClassName)}
          initial={{ width: 0 }}
          animate={{ width: `${clamped}%` }}
          transition={{ duration: 0.8, ease: 'easeOut' }}
        />
      </div>
    </div>
  );
};
