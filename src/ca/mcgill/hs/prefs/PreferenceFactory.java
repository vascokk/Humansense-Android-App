package ca.mcgill.hs.prefs;

import java.lang.reflect.InvocationTargetException;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.AttributeSet;
import ca.mcgill.hs.plugin.Plugin;

/**
 * An API allowing plugin programmers to easily generate preference objects.
 */
public class PreferenceFactory {

	public static final String PREFERENCES_CHANGED_INTENT = "ca.mcgill.hs.HSAndroidApp.PREFERENCES_CHANGED_INTENT";

	private static Context context = null;

	/**
	 * Broadcasts an intent whenever a preference has changed.
	 * 
	 * @param context
	 *            The context in which the intent will be broadcast.
	 */
	private static void broadcastIntent(final Context context) {
		final Intent i = new Intent();
		i.setAction(PREFERENCES_CHANGED_INTENT);
		context.sendBroadcast(i);
	}

	/**
	 * This method creates a PreferenceScreen from the available Plugins
	 * classes.
	 * 
	 * @param activity
	 *            A PreferenceActivity in which this screen will be displayed.
	 * @param plugins
	 *            A list of plugins to generate preferences for.
	 * @return A PreferenceScreen with the appropriate Preference objects.
	 */
	public static PreferenceScreen createPreferenceHierarchy(
			final PreferenceActivity activity,
			final Class<? extends Plugin>[] plugins) {
		final PreferenceScreen root = activity.getPreferenceManager()
				.createPreferenceScreen(activity);
		try {
			for (final Class<? extends Plugin> plugin : plugins) {
				// Check if the plugin has custom preferences
				if ((Boolean) plugin
						.getMethod("hasPreferences", new Class[] {}).invoke(
								null, new Object[] {})) {
					final PreferenceCategory newCategory = new PreferenceCategory(
							activity);
					newCategory.setTitle(plugin.getSimpleName()
							+ " Preferences");
					root.addPreference(newCategory);
					final Preference[] preferences = (Preference[]) plugin
							.getMethod("getPreferences",
									PreferenceActivity.class).invoke(null,
									activity);
					for (final Preference preference : preferences) {
						if (preference != null) {
							newCategory.addPreference(preference);
						}
					}
				}
			}
		} catch (final IllegalArgumentException e) {
			e.printStackTrace();
		} catch (final SecurityException e) {
			e.printStackTrace();
		} catch (final IllegalAccessException e) {
			e.printStackTrace();
		} catch (final InvocationTargetException e) {
			e.printStackTrace();
		} catch (final NoSuchMethodException e) {
			e.printStackTrace();
		}
		return root;
	}

	public static Preference getButtonPreference(
			final PreferenceActivity activity, final String key,
			final int titleResId, final int summaryDefResId) {
		final Preference buttonPref = new Preference(activity);
		buttonPref.setKey(key);
		buttonPref.setTitle(titleResId);
		buttonPref.setSummary(summaryDefResId);
		return buttonPref;
	}

	/**
	 * Returns a CheckBoxPreference preference specified by the parameters. This
	 * method does not use resource IDs for its parameters.
	 * 
	 * @param activity
	 *            The PreferenceActivity in which this preference will appear
	 * @param key
	 *            The key of this preference.
	 * @param titleResId
	 *            The resource ID for the title of this preference.
	 * @param summaryDefResId
	 *            The resource ID for the default summary of this preference.
	 * @param summaryOnResId
	 *            The resource ID for the summary displayed when this checkbox
	 *            is checked.
	 * @param summaryOffResId
	 *            The resource ID for the summary displayed when this checkbox
	 *            is unchecked.
	 * @return A CheckBoxPreference object.
	 */
	public static CheckBoxPreference getCheckBoxPreference(
			final PreferenceActivity activity, final String key,
			final int titleResId, final int summaryDefResId,
			final int summaryOnResId, final int summaryOffResId,
			final boolean defaultValue) {
		final CheckBoxPreference cbp = new CheckBoxPreference(activity);

		cbp.setKey(key);
		cbp.setTitle(titleResId);
		cbp.setSummary(summaryDefResId);
		cbp.setSummaryOn(summaryOnResId);
		cbp.setSummaryOff(summaryOffResId);
		cbp.setDefaultValue(defaultValue);

		cbp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(final Preference preference,
					final Object newValue) {
				cbp.setChecked((Boolean) newValue);
				broadcastIntent(context);
				return false;
			}
		});

		return cbp;
	}

	/**
	 * Returns a CheckBoxPreference preference specified by the parameters. This
	 * method does not use resource IDs for its parameters.
	 * 
	 * @param activity
	 *            The PreferenceActivity in which this preference will appear
	 * @param key
	 *            The key of this preference.
	 * @param title
	 *            The String for the title of this preference.
	 * @param summaryDef
	 *            The String for the default summary of this preference.
	 * @param summaryOn
	 *            The String for the summary displayed when this checkbox is
	 *            checked.
	 * @param summaryOff
	 *            The String for the summary displayed when this checkbox is
	 *            unchecked.
	 * @return A CheckBoxPreference object.
	 */
	public static CheckBoxPreference getCheckBoxPreference(
			final PreferenceActivity activity, final String key,
			final String title, final String summaryDef,
			final String summaryOn, final String summaryOff,
			final boolean defaultValue) {
		final CheckBoxPreference cbp = new CheckBoxPreference(activity);

		cbp.setKey(key);
		cbp.setTitle(title);
		cbp.setSummary(summaryDef);
		cbp.setSummaryOn(summaryOn);
		cbp.setSummaryOff(summaryOff);
		cbp.setDefaultValue(defaultValue);

		cbp.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(final Preference preference,
					final Object newValue) {
				cbp.setChecked((Boolean) newValue);
				broadcastIntent(context);
				return false;
			}
		});

		return cbp;
	}

	/**
	 * Returns a ListPreference preference specified by the parameters. This
	 * method uses resource IDs for its parameters.
	 * 
	 * @param activity
	 *            The PreferenceActivity in which this preference will appear
	 * @param entriesResId
	 *            The resource ID for the text entries of this preference.
	 * @param entryValuesResId
	 *            The resource ID for the entry values of this preference.
	 * @param defaultValue
	 *            The default value of this preference.
	 * @param key
	 *            The key of this preference.
	 * @param titleResId
	 *            The resource ID for the title of this preference.
	 * @param summaryResId
	 *            The resource ID for the summary of this preference.
	 * @return A ListPreference object.
	 */
	public static ListPreference getListPreference(
			final PreferenceActivity activity, final int entriesResId,
			final int entryValuesResId, final Object defaultValue,
			final String key, final int titleResId, final int summaryResId) {
		final ListPreference result = new ListPreference(activity);

		result.setEntries(entriesResId);
		result.setEntryValues(entryValuesResId);
		result.setDefaultValue(defaultValue);
		result.setKey(key);
		result.setTitle(titleResId);
		result.setSummary(summaryResId);

		result.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(final Preference preference,
					final Object newValue) {
				result.setValue((String) newValue);
				broadcastIntent(context);
				return false;
			}
		});

		return result;
	}

	/**
	 * Returns a ListPreference preference specified by the parameters. This
	 * method does not use resource IDs for its parameters.
	 * 
	 * @param activity
	 *            The PreferenceActivity in which this preference will appear
	 * @param entries
	 *            The String array of entries for this preference.
	 * @param entryValues
	 *            The String array of values for this preference.
	 * @param defaultValue
	 *            The default value of this preference.
	 * @param key
	 *            The key of this preference.
	 * @param title
	 *            The String for the title of this preference.
	 * @param summary
	 *            The String for the summary of this preference.
	 * @return A ListPreference object.
	 */
	public static ListPreference getListPreference(
			final PreferenceActivity activity, final String[] entries,
			final String[] entryValues, final Object defaultValue,
			final String key, final String title, final String summary) {
		final ListPreference result = new ListPreference(activity);

		result.setEntries(entries);
		result.setEntryValues(entryValues);
		result.setDefaultValue(defaultValue);
		result.setKey(key);
		result.setTitle(title);
		result.setSummary(summary);

		result.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(final Preference preference,
					final Object newValue) {
				result.setValue((String) newValue);
				broadcastIntent(context);
				return false;
			}
		});

		return result;
	}

	/**
	 * Returns a SeekBarPreference.
	 * 
	 * @param activity
	 *            The PreferenceActivity in which this preference will appear
	 * @param attrs
	 *            The attributes for this seekbar
	 * @param key
	 *            The unique key that identifies this preference
	 * @return A new SeekBarPreference with the specified attributes.
	 */
	public static SeekBarPreference getSeekBarPreference(
			final PreferenceActivity activity, final AttributeSet attrs,
			final String key) {
		final SeekBarPreference result = new SeekBarPreference(activity, attrs);
		result.setKey(key);

		result.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			public boolean onPreferenceChange(final Preference preference,
					final Object newValue) {
				// result.setValue((String) newValue);
				broadcastIntent(context);
				return false;
			}
		});

		return result;
	}

	public static SharedPreferences getSharedPreferences() {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	/**
	 * IMPORTANT: This method must be called before using this factory!
	 * 
	 * @param context
	 *            The context in which these preferences live.
	 */
	public static void setContext(final Context context) {
		PreferenceFactory.context = context;
	}

}