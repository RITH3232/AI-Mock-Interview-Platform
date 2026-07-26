import * as React from 'react';
import { cn } from '../../utils/utils';

export const Skeleton: React.FC<React.HTMLAttributes<HTMLDivElement>> = ({ className, ...props }) => (
  <div
    className={cn('animate-pulse rounded-xl bg-accent/60', className)}
    {...props}
  />
);
