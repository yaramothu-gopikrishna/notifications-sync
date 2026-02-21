import { useState, useEffect, useCallback } from 'react';
import { Bell, ChevronLeft, ChevronRight, RefreshCw } from 'lucide-react';
import { listNotifications } from '../api/notifications';
import StatusBadge from '../components/StatusBadge';
import EmptyState from '../components/EmptyState';
import LoadingSpinner from '../components/LoadingSpinner';
import toast from 'react-hot-toast';

export default function NotificationsPage() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const pageSize = 15;

  const fetchNotifs = useCallback(async () => {
    try {
      const { data } = await listNotifications(page, pageSize);
      setNotifications(Array.isArray(data) ? data : []);
    } catch {
      toast.error('Failed to load notifications');
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { fetchNotifs(); }, [fetchNotifs]);

  // Auto-refresh every 30s
  useEffect(() => {
    const timer = setInterval(fetchNotifs, 30000);
    return () => clearInterval(timer);
  }, [fetchNotifs]);

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Notification History</h1>
        <button onClick={fetchNotifs}
          className="flex items-center gap-2 px-3 py-2 text-gray-600 hover:bg-gray-100 rounded-lg cursor-pointer text-sm">
          <RefreshCw size={14} /> Refresh
        </button>
      </div>

      {notifications.length === 0 ? (
        <EmptyState icon={Bell} title="No notifications yet"
          description="Notifications will appear here once your connected email receives new messages" />
      ) : (
        <>
          <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
            <table className="w-full">
              <thead className="bg-gray-50 border-b">
                <tr>
                  <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">From</th>
                  <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">Subject</th>
                  <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase hidden md:table-cell">Preview</th>
                  <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase">Status</th>
                  <th className="text-left px-4 py-3 text-xs font-medium text-gray-500 uppercase hidden sm:table-cell">Time</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {notifications.map((n) => (
                  <tr key={n.id} className="hover:bg-gray-50">
                    <td className="px-4 py-3">
                      <div className="text-sm font-medium text-gray-900">{n.senderName || n.sender_name}</div>
                      <div className="text-xs text-gray-500">{n.senderAddress || n.sender_address}</div>
                    </td>
                    <td className="px-4 py-3 text-sm text-gray-900 max-w-xs truncate">{n.subject}</td>
                    <td className="px-4 py-3 text-sm text-gray-500 max-w-xs truncate hidden md:table-cell">{n.preview}</td>
                    <td className="px-4 py-3"><StatusBadge status={n.deliveryStatus || n.delivery_status} /></td>
                    <td className="px-4 py-3 text-xs text-gray-500 hidden sm:table-cell">
                      {n.createdAt ? new Date(n.createdAt).toLocaleString() : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          <div className="flex items-center justify-between mt-4">
            <button onClick={() => setPage(Math.max(0, page - 1))} disabled={page === 0}
              className="flex items-center gap-1 px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg disabled:opacity-50 cursor-pointer">
              <ChevronLeft size={14} /> Previous
            </button>
            <span className="text-sm text-gray-500">Page {page + 1}</span>
            <button onClick={() => setPage(page + 1)} disabled={notifications.length < pageSize}
              className="flex items-center gap-1 px-3 py-2 text-sm text-gray-600 hover:bg-gray-100 rounded-lg disabled:opacity-50 cursor-pointer">
              Next <ChevronRight size={14} />
            </button>
          </div>
        </>
      )}
    </div>
  );
}
