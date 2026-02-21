import { useState, useEffect } from 'react';
import { MessageSquare, Plus, Trash2, Pencil } from 'lucide-react';
import { listChannels, createChannel, updateChannel, deleteChannel } from '../api/channels';
import StatusBadge from '../components/StatusBadge';
import EmptyState from '../components/EmptyState';
import LoadingSpinner from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import toast from 'react-hot-toast';
import { getErrorMessage } from '../utils/errorUtils';

export default function ChannelsPage() {
  const [channels, setChannels] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const [editChannel, setEditChannel] = useState(null);
  const [form, setForm] = useState({ channelType: 'slack', botToken: '', slackChannelId: '', whatsappPhoneNumber: '', consentGiven: false });

  const fetchChannels = async () => {
    try {
      const { data } = await listChannels();
      setChannels(Array.isArray(data) ? data : []);
    } catch (err) {
      toast.error(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchChannels(); }, []);

  const resetForm = () => setForm({ channelType: 'slack', botToken: '', slackChannelId: '', whatsappPhoneNumber: '', consentGiven: false });

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await createChannel(form);
      toast.success('Channel added!');
      setShowAdd(false);
      resetForm();
      fetchChannels();
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  };

  const openEdit = (ch) => {
    setEditChannel(ch);
    setForm({
      channelType: ch.channelType || ch.channel_type,
      botToken: '',
      slackChannelId: ch.slackChannelId || ch.slack_channel_id || '',
      whatsappPhoneNumber: ch.whatsappPhoneNumber || ch.whatsapp_phone_number || '',
      consentGiven: ch.consentGiven ?? ch.consent_given ?? false,
    });
  };

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      await updateChannel(editChannel.id, form);
      toast.success('Channel updated!');
      setEditChannel(null);
      resetForm();
      fetchChannels();
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Remove this notification channel?')) return;
    try {
      await deleteChannel(id);
      toast.success('Channel removed');
      fetchChannels();
    } catch (err) {
      toast.error(getErrorMessage(err));
    }
  };

  if (loading) return <LoadingSpinner />;

  const channelForm = (onSubmit, submitLabel) => (
    <form onSubmit={onSubmit} className="space-y-4">
      {!editChannel && (
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Channel Type</label>
          <select value={form.channelType} onChange={(e) => setForm({ ...form, channelType: e.target.value })}
            className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500">
            <option value="slack">Slack</option>
            <option value="whatsapp">WhatsApp</option>
          </select>
        </div>
      )}

      {form.channelType === 'slack' ? (
        <>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Bot Token</label>
            <input type="text" value={form.botToken} onChange={(e) => setForm({ ...form, botToken: e.target.value })}
              placeholder={editChannel ? 'Leave blank to keep current' : 'xoxb-...'}
              required={!editChannel}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Slack Channel ID</label>
            <input type="text" required value={form.slackChannelId} onChange={(e) => setForm({ ...form, slackChannelId: e.target.value })}
              placeholder="C0123456789" className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
        </>
      ) : (
        <>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">WhatsApp Phone Number</label>
            <input type="tel" required value={form.whatsappPhoneNumber} onChange={(e) => setForm({ ...form, whatsappPhoneNumber: e.target.value })}
              placeholder="+1234567890" className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <label className="flex items-center gap-2 text-sm text-gray-700">
            <input type="checkbox" checked={form.consentGiven} onChange={(e) => setForm({ ...form, consentGiven: e.target.checked })}
              className="rounded border-gray-300" />
            I consent to receive WhatsApp notifications
          </label>
        </>
      )}

      <button type="submit"
        className="w-full py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 cursor-pointer font-medium">
        {submitLabel}
      </button>
    </form>
  );

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Notification Channels</h1>
        <button onClick={() => { resetForm(); setShowAdd(true); }}
          className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 cursor-pointer text-sm font-medium">
          <Plus size={16} /> Add Channel
        </button>
      </div>

      {channels.length === 0 ? (
        <EmptyState icon={MessageSquare} title="No notification channels"
          description="Add Slack or WhatsApp to receive email notifications"
          action={
            <button onClick={() => { resetForm(); setShowAdd(true); }}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 cursor-pointer text-sm">
              Add Channel
            </button>
          } />
      ) : (
        <div className="space-y-3">
          {channels.map((ch) => (
            <div key={ch.id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 flex items-center justify-between">
              <div className="flex items-center gap-4">
                <div className={`p-2 rounded-lg ${ch.channelType === 'slack' ? 'bg-purple-100' : 'bg-green-100'}`}>
                  <MessageSquare size={20} className={ch.channelType === 'slack' ? 'text-purple-600' : 'text-green-600'} />
                </div>
                <div>
                  <p className="font-medium text-gray-900 capitalize">{ch.channelType || ch.channel_type}</p>
                  <p className="text-xs text-gray-500">
                    {ch.channelType === 'slack' ? `Channel: ${ch.slackChannelId || ch.slack_channel_id || '—'}` : `Phone: ${ch.whatsappPhoneNumber || ch.whatsapp_phone_number || '—'}`}
                  </p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <StatusBadge status={ch.status} />
                <button onClick={() => openEdit(ch)} title="Edit"
                  className="p-2 text-gray-600 hover:bg-gray-50 rounded-lg cursor-pointer">
                  <Pencil size={16} />
                </button>
                <button onClick={() => handleDelete(ch.id)} title="Remove"
                  className="p-2 text-red-600 hover:bg-red-50 rounded-lg cursor-pointer">
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal open={showAdd} onClose={() => setShowAdd(false)} title="Add Notification Channel">
        {channelForm(handleCreate, 'Add Channel')}
      </Modal>

      <Modal open={!!editChannel} onClose={() => { setEditChannel(null); resetForm(); }} title="Edit Notification Channel">
        {channelForm(handleUpdate, 'Save Changes')}
      </Modal>
    </div>
  );
}
