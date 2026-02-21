import { useState, useEffect } from 'react';
import { Mail, Pause, Play, Trash2, Plus } from 'lucide-react';
import { listAccounts, connectGmail, pauseAccount, resumeAccount, disconnectAccount } from '../api/emailAccounts';
import StatusBadge from '../components/StatusBadge';
import EmptyState from '../components/EmptyState';
import LoadingSpinner from '../components/LoadingSpinner';
import toast from 'react-hot-toast';

export default function EmailAccountsPage() {
  const [accounts, setAccounts] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchAccounts = async () => {
    try {
      const { data } = await listAccounts();
      setAccounts(Array.isArray(data) ? data : []);
    } catch {
      toast.error('Failed to load accounts');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchAccounts(); }, []);

  const handleConnect = async () => {
    try {
      const { data } = await connectGmail();
      window.open(data.authorizationUrl, '_blank');
      toast.success('Complete authorization in the new tab');
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to start connection. Configure GOOGLE_CLIENT_ID first.');
    }
  };

  const handlePause = async (id) => {
    try {
      await pauseAccount(id);
      toast.success('Account paused');
      fetchAccounts();
    } catch {
      toast.error('Failed to pause');
    }
  };

  const handleResume = async (id) => {
    try {
      await resumeAccount(id);
      toast.success('Account resumed');
      fetchAccounts();
    } catch {
      toast.error('Failed to resume');
    }
  };

  const handleDisconnect = async (id) => {
    if (!confirm('Disconnect this email account? This will stop all scanning.')) return;
    try {
      await disconnectAccount(id);
      toast.success('Account disconnected');
      fetchAccounts();
    } catch {
      toast.error('Failed to disconnect');
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Email Accounts</h1>
        <button onClick={handleConnect}
          className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 cursor-pointer text-sm font-medium">
          <Plus size={16} /> Connect Gmail
        </button>
      </div>

      {accounts.length === 0 ? (
        <EmptyState icon={Mail} title="No email accounts connected"
          description="Connect your Gmail account to start receiving notifications"
          action={
            <button onClick={handleConnect}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 cursor-pointer text-sm">
              Connect Gmail
            </button>
          } />
      ) : (
        <div className="space-y-3">
          {accounts.map((acc) => (
            <div key={acc.id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 flex items-center justify-between">
              <div className="flex items-center gap-4">
                <div className="bg-red-100 p-2 rounded-lg">
                  <Mail size={20} className="text-red-600" />
                </div>
                <div>
                  <p className="font-medium text-gray-900">{acc.emailAddress || acc.email_address}</p>
                  <p className="text-xs text-gray-500">
                    Provider: {acc.provider || 'gmail'} · Last scanned: {acc.lastScannedAt ? new Date(acc.lastScannedAt).toLocaleString() : 'Never'}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <StatusBadge status={acc.status} />
                {acc.status === 'active' && (
                  <button onClick={() => handlePause(acc.id)} title="Pause"
                    className="p-2 text-yellow-600 hover:bg-yellow-50 rounded-lg cursor-pointer">
                    <Pause size={16} />
                  </button>
                )}
                {acc.status === 'paused' && (
                  <button onClick={() => handleResume(acc.id)} title="Resume"
                    className="p-2 text-green-600 hover:bg-green-50 rounded-lg cursor-pointer">
                    <Play size={16} />
                  </button>
                )}
                <button onClick={() => handleDisconnect(acc.id)} title="Disconnect"
                  className="p-2 text-red-600 hover:bg-red-50 rounded-lg cursor-pointer">
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
