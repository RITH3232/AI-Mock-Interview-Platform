import * as React from 'react';
import { Check } from 'lucide-react';
import { cn } from '../../utils/utils';

export interface ChipProps {
  label: string;
  selected: boolean;
  onClick: () => void;
  className?: string;
}

export const Chip: React.FC<ChipProps> = ({ label, selected, onClick, className }) => (
  <button
    type="button"
    onClick={onClick}
    aria-pressed={selected}
    className={cn(
      'inline-flex items-center gap-1.5 px-3.5 py-2 rounded-full text-sm font-medium border transition-all duration-150 select-none',
      selected
        ? 'bg-primary text-primary-foreground border-primary shadow-sm shadow-primary/25'
        : 'bg-card text-muted-foreground border-border hover:border-primary/50 hover:text-foreground',
      className
    )}
  >
    {selected && <Check className="w-3.5 h-3.5" />}
    {label}
  </button>
);
