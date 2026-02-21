import { useState, useEffect } from 'react';
import { Filter, Plus, Trash2, ToggleLeft, ToggleRight } from 'lucide-react';
import { listRules, createRule, updateRule, deleteRule } from '../api/filterRules';
import StatusBadge from '../components/StatusBadge';
import EmptyState from '../components/EmptyState';
import LoadingSpinner from '../components/LoadingSpinner';
import Modal from '../components/Modal';
import toast from 'react-hot-toast';

export default function FiltersPage() {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showAdd, setShowAdd] = useState(false);
  const [form, setForm] = useState({ ruleType: 'sender', pattern: '', active: true, priority: 0 });

  const fetchRules = async () => {
    try {
      const { data } = await listRules();
      setRules(Array.isArray(data) ? data : []);
    } catch {
      toast.error('Failed to load filter rules');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { fetchRules(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await createRule(form);
      toast.success('Filter rule created!');
      setShowAdd(false);
      setForm({ ruleType: 'sender', pattern: '', active: true, priority: 0 });
      fetchRules();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create rule');
    }
  };

  const handleToggle = async (rule) => {
    try {
      await updateRule(rule.id, { ...rule, active: !rule.active });
      fetchRules();
    } catch {
      toast.error('Failed to update rule');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this filter rule?')) return;
    try {
      await deleteRule(id);
      toast.success('Rule deleted');
      fetchRules();
    } catch {
      toast.error('Failed to delete');
    }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Filter Rules</h1>
          <p className="text-sm text-gray-500 mt-1">
            {rules.length === 0 ? 'No filters — all new emails will trigger notifications' : 'Only matching emails will trigger notifications'}
          </p>
        </div>
        <button onClick={() => setShowAdd(true)}
          className="flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 cursor-pointer text-sm font-medium">
          <Plus size={16} /> Add Rule
        </button>
      </div>

      {rules.length === 0 ? (
        <EmptyState icon={Filter} title="No filter rules"
          description="Without filters, you'll be notified for every new email. Add rules to filter by sender or subject."
          action={
            <button onClick={() => setShowAdd(true)}
              className="px-4 py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 cursor-pointer text-sm">
              Add Rule
            </button>
          } />
      ) : (
        <div className="space-y-3">
          {rules.map((rule) => (
            <div key={rule.id} className="bg-white rounded-xl shadow-sm border border-gray-200 p-4 flex items-center justify-between">
              <div className="flex items-center gap-4">
                <div className={`p-2 rounded-lg ${rule.ruleType === 'sender' ? 'bg-blue-100' : 'bg-amber-100'}`}>
                  <Filter size={20} className={rule.ruleType === 'sender' ? 'text-blue-600' : 'text-amber-600'} />
                </div>
                <div>
                  <p className="font-medium text-gray-900">
                    <span className="capitalize">{rule.ruleType || rule.rule_type}</span>: <code className="bg-gray-100 px-1.5 py-0.5 rounded text-sm">{rule.pattern}</code>
                  </p>
                  <p className="text-xs text-gray-500">Priority: {rule.priority}</p>
                </div>
              </div>
              <div className="flex items-center gap-3">
                <StatusBadge status={rule.active ? 'active' : 'paused'} />
                <button onClick={() => handleToggle(rule)} title="Toggle"
                  className="p-2 text-gray-600 hover:bg-gray-50 rounded-lg cursor-pointer">
                  {rule.active ? <ToggleRight size={20} className="text-green-600" /> : <ToggleLeft size={20} className="text-gray-400" />}
                </button>
                <button onClick={() => handleDelete(rule.id)} title="Delete"
                  className="p-2 text-red-600 hover:bg-red-50 rounded-lg cursor-pointer">
                  <Trash2 size={16} />
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <Modal open={showAdd} onClose={() => setShowAdd(false)} title="Add Filter Rule">
        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Rule Type</label>
            <select value={form.ruleType} onChange={(e) => setForm({ ...form, ruleType: e.target.value })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500">
              <option value="sender">Sender Email</option>
              <option value="subject_keyword">Subject Keyword</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Pattern</label>
            <input type="text" required value={form.pattern} onChange={(e) => setForm({ ...form, pattern: e.target.value })}
              placeholder={form.ruleType === 'sender' ? 'boss@company.com' : 'urgent'}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Priority (lower = higher)</label>
            <input type="number" value={form.priority} onChange={(e) => setForm({ ...form, priority: parseInt(e.target.value) || 0 })}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <button type="submit"
            className="w-full py-2 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 cursor-pointer font-medium">
            Create Rule
          </button>
        </form>
      </Modal>
    </div>
  );
}
