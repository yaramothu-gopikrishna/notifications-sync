import { Loader2 } from 'lucide-react';

export default function LoadingSpinner({ size = 24 }) {
  return (
    <div className="flex items-center justify-center py-12">
      <Loader2 size={size} className="animate-spin text-indigo-600" />
    </div>
  );
}
