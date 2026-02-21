import { useState, useEffect } from 'react';
import { Mail, MessageSquare, Bell, ArrowRight } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { listAccounts } from '../api/emailAccounts';
import { listChannels } from '../api/channels';
import { listNotifications } from '../api/notifications';
import LoadingSpinner from '../components/LoadingSpinner';

export default function DashboardPage() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    Promise.all([
      listAccounts().catch(() => ({ data: [] })),
      listChannels().catch(() => ({ data: [] })),
      listNotifications(0, 5).catch(() => ({ data: [] })),
    ]).then(([accounts, channels, notifs]) => {
      setStats({
        accounts: Array.isArray(accounts.data) ? accounts.data : [],
        channels: Array.isArray(channels.data) ? channels.data : [],
        notifications: Array.isArray(notifs.data) ? notifs.data : [],
      });
      setLoading(false);
    });
  }, []);

  if (loading) return <LoadingSpinner />;

  const cards = [
    {
      label: 'Email Accounts',
      count: stats.accounts.length,
      icon: Mail,
      color: 'bg-blue-500',
      link: '/email-accounts',
    },
    {
      label: 'Notification Channels',
      count: stats.channels.length,
      icon: MessageSquare,
      color: 'bg-green-500',
      link: '/channels',
    },
    {
      label: 'Recent Notifications',
      count: stats.notifications.length,
      icon: Bell,
      color: 'bg-purple-500',
      link: '/notifications',
    },
  ];

  return (
    <div>
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
        {cards.map((card) => (
          <div key={card.label}
            className="bg-white rounded-xl shadow-sm border border-gray-200 p-6 hover:shadow-md transition-shadow cursor-pointer"
            onClick={() => navigate(card.link)}
          >
            <div className="flex items-center justify-between">
              <div>
                <p className="text-sm text-gray-500">{card.label}</p>
                <p className="text-3xl font-bold text-gray-900 mt-1">{card.count}</p>
              </div>
              <div className={`${card.color} p-3 rounded-lg`}>
                <card.icon size={24} className="text-white" />
              </div>
            </div>
            <div className="mt-4 flex items-center text-sm text-indigo-600">
              View all <ArrowRight size={14} className="ml-1" />
            </div>
          </div>
        ))}
      </div>

      <div className="bg-white rounded-xl shadow-sm border border-gray-200 p-6">
        <h2 className="text-lg font-semibold mb-4">Quick Setup Guide</h2>
        <div className="space-y-3">
          <Step num={1} done={stats.accounts.length > 0}
            label="Connect your Gmail account" action={() => navigate('/email-accounts')} />
          <Step num={2} done={stats.channels.length > 0}
            label="Add a notification channel (Slack or WhatsApp)" action={() => navigate('/channels')} />
          <Step num={3} done={stats.notifications.length > 0}
            label="Receive notifications when new emails arrive" />
        </div>
      </div>
    </div>
  );
}

function Step({ num, done, label, action }) {
  return (
    <div className={`flex items-center gap-3 p-3 rounded-lg ${done ? 'bg-green-50' : 'bg-gray-50'}`}>
      <span className={`flex items-center justify-center w-7 h-7 rounded-full text-sm font-bold
        ${done ? 'bg-green-500 text-white' : 'bg-gray-300 text-white'}`}>
        {done ? '✓' : num}
      </span>
      <span className={`flex-1 text-sm ${done ? 'text-green-700 line-through' : 'text-gray-700'}`}>{label}</span>
      {action && !done && (
        <button onClick={action}
          className="text-sm text-indigo-600 hover:text-indigo-500 font-medium cursor-pointer">
          Set up →
        </button>
      )}
    </div>
  );
}
