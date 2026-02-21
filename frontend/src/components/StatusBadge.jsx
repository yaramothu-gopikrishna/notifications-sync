const colors = {
  active: 'bg-green-100 text-green-800',
  sent: 'bg-green-100 text-green-800',
  delivered: 'bg-green-100 text-green-800',
  paused: 'bg-yellow-100 text-yellow-800',
  batched: 'bg-yellow-100 text-yellow-800',
  pending: 'bg-blue-100 text-blue-800',
  error: 'bg-red-100 text-red-800',
  failed: 'bg-red-100 text-red-800',
  disconnected: 'bg-gray-100 text-gray-800',
};

export default function StatusBadge({ status }) {
  const cls = colors[status] || 'bg-gray-100 text-gray-800';
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${cls}`}>
      {status}
    </span>
  );
}
