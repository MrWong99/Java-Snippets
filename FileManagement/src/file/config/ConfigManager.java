package file.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;

/**
 * This class is used to store and load key-value pairs. They can be saved and
 * loaded from a local file. Only one ConfigManager can be used for each file.
 * 
 * @author Lukas Schmidt
 */
public class ConfigManager {
	private HashMap<String, String> configurations;
	private File saveLocation;
	private String splitPattern = ": ";
	private boolean saveOnGC = false;

	private static ArrayList<String> configsUsed = new ArrayList<>();

	/**
	 * Creates a ConfigManager if there isn't already one in use for given file.
	 * A ConfigManager is used to store and load key-value pairs. They can be
	 * saved and loaded from a local file.
	 * 
	 * @param saveLocation
	 *            The relative or absolute path you want to set as your
	 *            save-location.
	 */
	public ConfigManager(String saveLocation) {
		File in = new File(saveLocation);

		updateSaveLocation(in);
		configurations = new HashMap<>();
	}

	/**
	 * Creates a ConfigManager if there isn't already one in use for given file.
	 * A ConfigManager is used to store and load key-value pairs. They can be
	 * saved and loaded from a local file.
	 * 
	 * @param saveLocation
	 *            The relative or absolute path you want to set as your
	 *            save-location.
	 * @param setting
	 *            The key-value pairs you want to use as initial setting.
	 */
	public ConfigManager(String saveLocation, HashMap<String, String> setting) {
		File in = new File(saveLocation);

		updateSaveLocation(in);
		setSetting(setting);
	}

	/**
	 * Creates a ConfigManager if there isn't already one in use for given file.
	 * A ConfigManager is used to store and load key-value pairs. They can be
	 * saved and loaded from a local file.
	 * 
	 * @param saveLocation
	 *            The file were you want to store your settings.
	 */
	public ConfigManager(File saveLocation) {
		updateSaveLocation(saveLocation);
		configurations = new HashMap<>();
	}

	/**
	 * Creates a ConfigManager if there isn't already one in use for given file.
	 * A ConfigManager is used to store and load key-value pairs. They can be
	 * saved and loaded from a local file.
	 * 
	 * @param saveLocation
	 *            The file were you want to store your settings.
	 * @param setting
	 *            The key-value pairs you want to use as initial setting.
	 */
	public ConfigManager(File saveLocation, HashMap<String, String> setting) {
		updateSaveLocation(saveLocation);
		setSetting(setting);
	}

	/**
	 * Returns the value to which the specified key is mapped, or null if this
	 * map contains no mapping for the key.
	 * 
	 * @param key
	 *            The key whose associated value is to be returned.
	 * @return The value to which the specified key is mapped, or null if this
	 *         map contains no mapping for the key.
	 * @see HashMap#get(Object)
	 */
	public String get(String key) {
		return configurations.get(key);
	}

	/**
	 * Associates the specified value with the specified key in this map. If the
	 * map previously contained a mapping for the key, the old value is
	 * replaced.
	 * 
	 * @param key
	 *            Key with which the specified value is to be associated. Can
	 *            only contain word charater's [a-zA-Z_0-9]
	 * @param value
	 *            Value to be associated with the specified key. Can only
	 *            contain word charater's [a-zA-Z_0-9]
	 * @throws IllegalArgumentException
	 *             If any of the arguments contains non word character's.
	 *             Allowed: [a-zA-Z_0-9]
	 * @see HashMap#put(Object, Object)
	 */
	public void put(String key, String value) {
		if (!key.matches("\\w+")) {
			throw new IllegalArgumentException("Key " + key
					+ " contains non word character's. Allowed: [a-zA-Z_0-9]");
		}
		if (!value.matches("\\w+")) {
			throw new IllegalArgumentException("Value " + value
					+ " contains non word character's. Allowed: [a-zA-Z_0-9]");
		}

		configurations.put(key, value);
	}

	/**
	 * Saves current setting to predefined save-location. The file will have the
	 * format:<br>
	 * Value1 + {@link ConfigManager#getSplitPattern()} + Key1<br>
	 * Value2 + {@link ConfigManager#getSplitPattern()} + Key2
	 * 
	 * @return true if the operation was successful.
	 * @throws IOException
	 *             If any read-write issues occurred. Read the stack-trace for
	 *             more information.
	 */
	public synchronized boolean writeToSave() throws IOException {
		if (saveLocation.exists()) {
			if (saveLocation.delete()) {
				saveLocation.createNewFile();
			} else {
				return false;
			}
		}

		PrintWriter pw;
		pw = new PrintWriter(new FileOutputStream(saveLocation, true));
		Iterator<Entry<String, String>> it = configurations.entrySet()
				.iterator();

		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry pairs = (Map.Entry) it.next();
			pw.println(pairs.getKey() + splitPattern + pairs.getValue());
			it.remove();
		}
		pw.close();

		return true;
	}

	/**
	 * Loads every key-value pair from the predefined save-file. For each pair
	 * the {@link ConfigManager#put(String, String)} will be called. This means
	 * that new pairs will be added and already existing ones will be updated.
	 * 
	 * @return true if the operation was successful.
	 * @throws FileNotFoundException
	 *             If the file wasn't found.
	 * @throws IllegalArgumentException
	 *             If a line in the file does not match the pattern<br>
	 *             Key + {@link ConfigManager#getSplitPattern()} + Value
	 */
	public synchronized boolean loadFromSave() throws FileNotFoundException {
		Scanner sc = new Scanner(new FileInputStream(saveLocation));
		int i = 0;
		while (sc.hasNextLine()) {
			i++;
			String line = sc.nextLine();
			if (!line.matches("\\w+" + splitPattern + "\\w+")) {
				sc.close();
				throw new IllegalArgumentException("Line " + i
						+ " does not match pattern " + "\\w+" + splitPattern
						+ "\\w+");
			}

			String[] in = line.split(splitPattern);
			put(in[0], in[1]);
		}

		sc.close();

		return true;
	}

	/**
	 * Returns a new HashMap containing every key-value pair that is currently
	 * in use.
	 * 
	 * @return A new {@link HashMap}.
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, String> getSetting() {
		return (HashMap<String, String>) configurations.clone();
	}

	/**
	 * Replaces all current settings with the given setting. Note: This will not
	 * override any files. So calling {@link ConfigManager#loadFromSave()}
	 * before calling {@link ConfigManager#writeToSave()} will just load any
	 * previous saved settings.
	 * 
	 * @param setting
	 *            The new {@link HashMap} you want to replace current setting
	 *            with.
	 */
	@SuppressWarnings("unchecked")
	public void setSetting(HashMap<String, String> setting) {
		this.configurations = (HashMap<String, String>) setting.clone();
	}

	/**
	 * Returns a {@link File}-object for the current save-location.
	 */
	public File getSaveLocation() {
		return saveLocation;
	}

	/**
	 * Updates the save-location to this file. This will change the load and
	 * save operations destinations.
	 * 
	 * @throws IllegalArgumentException
	 *             If this file is already used by another {@link ConfigManager}
	 */
	public synchronized void updateSaveLocation(File saveLocation) {
		if (checkAndAdd(saveLocation.getAbsolutePath())) {
			configsUsed.remove(saveLocation.getAbsolutePath());
			this.saveLocation = new File(saveLocation.getPath());
		} else {
			throw new IllegalArgumentException(
					"There already is a ConfigManager for "
							+ saveLocation.getAbsolutePath());
		}
	}

	/**
	 * Returns the current pattern used to split the keys from the values in the
	 * save-file. The default pattern is Key: Value
	 */
	public String getSplitPattern() {
		return splitPattern;
	}

	/**
	 * Sets the pattern used to split the key from the values in the save-file.
	 * The default pattern is Key: Value
	 * 
	 * @throws IllegalArgumentException
	 *             If the argument is empty or null.
	 */
	public void setSplitPattern(String splitPattern) {
		if (splitPattern.equals("") || splitPattern == null) {
			throw new IllegalArgumentException(
					"Splitpattern can't be null or empty.");
		}
		this.splitPattern = splitPattern;
	}

	/**
	 * Returns an array that contains any absolute path used by a
	 * {@link ConfigManager} as save-location.
	 */
	public String[] getCurrentlyUsedLocations() {
		String[] res = new String[configsUsed.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = configsUsed.get(i);
		}

		return res;
	}

	/**
	 * Returns the current saving mechanism when
	 * {@link ConfigManager#finalize()} is called. If true the setting will be
	 * saved to the current save-location else nothing will happen with the
	 * file.
	 */
	public boolean isSavedOnGC() {
		return saveOnGC;
	}

	/**
	 * Sets the mechanism {@link ConfigManager#finalize()} is called. If true
	 * the setting will be saved to the current save-location else nothing will
	 * happen with the file.
	 */
	public void setSaveOnGC(boolean saveOnGC) {
		this.saveOnGC = saveOnGC;
	}

	/**
	 * Checks if there already is a {@link ConfigManager} initialized for this
	 * save-location. If not will return true and set this path as used.
	 * 
	 * @param filePath
	 *            The absolute path of the save-file.
	 * @return true if no ConfigManager uses this save-location.
	 */
	private synchronized boolean checkAndAdd(String filePath) {
		if (configsUsed.contains(filePath)) {
			return false;
		} else {
			configsUsed.add(filePath);
			return true;
		}
	}

	@Override
	protected void finalize() throws Throwable {
		if (saveOnGC) {
			writeToSave();
		}
		configsUsed.remove(saveLocation.getAbsolutePath());
	}
}