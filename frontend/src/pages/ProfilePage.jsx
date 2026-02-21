import { useAuth } from '../context/AuthContext';
import { User } from 'lucide-react';

export default function ProfilePage() {
  const { user } = useAuth();

  if (!user) return null;

  return (
    <div className="max-w-lg mx-auto">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Profile</h1>

      <div className="bg-white rounded-2xl shadow-lg p-8 space-y-6">
        <div className="flex items-center gap-4">
          {user.gravatarUrl ? (
            <img
              src={user.gravatarUrl}
              alt="Avatar"
              className="w-16 h-16 rounded-full"
            />
          ) : (
            <div className="w-16 h-16 bg-indigo-100 rounded-full flex items-center justify-center">
              <User size={32} className="text-indigo-600" />
            </div>
          )}
          <div>
            <p className="text-lg font-semibold text-gray-900">{user.email}</p>
            <p className="text-sm text-gray-500">
              Member since {new Date(user.createdAt).toLocaleDateString()}
            </p>
          </div>
        </div>

        <div className="border-t pt-4 space-y-3">
          <div className="flex justify-between items-center">
            <span className="text-gray-600">Email</span>
            <span className="font-medium text-gray-900">{user.email}</span>
          </div>
          <div className="flex justify-between items-center">
            <span className="text-gray-600">Notifications</span>
            <span className={`px-2 py-0.5 rounded-full text-xs font-medium ${
              user.notificationsPaused
                ? 'bg-yellow-100 text-yellow-800'
                : 'bg-green-100 text-green-800'
            }`}>
              {user.notificationsPaused ? 'Paused' : 'Active'}
            </span>
          </div>
        </div>

        <p className="text-xs text-gray-400">
          Your avatar is powered by <a href="https://gravatar.com" target="_blank" rel="noopener noreferrer" className="underline">Gravatar</a>.
          Update it by associating an image with your email on Gravatar.
        </p>
      </div>
    </div>
  );
}
