import * as React from 'react';
import { LucideIcon } from 'lucide-react';
import { cn } from '../../utils/utils';

export interface EmptyStateProps {
  icon: LucideIcon;
  title: string;
  description?: string;
  action?: React.ReactNode;
  className?: string;
}

export const EmptyState: React.FC<EmptyStateProps> = ({ icon: Icon, title, description, action, className }) => (
  <div className={cn('flex flex-col items-center justify-center text-center py-14 px-6', className)}>
    <div className="p-4 rounded-2xl bg-accent/50 border border-border/50 text-muted-foreground mb-4">
      <Icon className="w-7 h-7" />
    </div>
    <h3 className="font-bold text-foreground">{title}</h3>
    {description && <p className="text-sm text-muted-foreground mt-1.5 max-w-sm">{description}</p>}
    {action && <div className="mt-5">{action}</div>}
  </div>
);
