package pl.scisoftware.bpm.jaxws.config;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.lang.StringUtils;

import com.ibm.websphere.wim.ras.WIMLogger;

import pl.scisoftware.bpm.jaxws.config.api.IConfigLoader;

public class ConfigLoader implements IConfigLoader {

	public static final String CLASSNAME = ConfigLoader.class.getName();
	private static final Logger logger = WIMLogger.getTraceLogger(CLASSNAME);
	public static final String CONFIGURATION_FILE_NAME = "cmis-repositories.properties";

	private static Map<String, String> systemVars = null;
	private static final Object systemVarsLock = new Object();
	private final String configurationDir;

	public ConfigLoader(String configurationDir) {
		this.configurationDir = configurationDir;
	}

	/**
	 * Ładowania parametrów konfiguracyjnych systemu.
	 * 
	 * @return mapa parametrów
	 */
	@Override
	public Map<String, String> loadConfiguration() {
		synchronized (systemVarsLock) {
			if (systemVars == null) {
				systemVars = loadConfiguration(true);
			}
			return systemVars;
		}
	}

	/**
	 * Ładowania/przeładowanie parametrów konfiguracyjnych systemu.
	 * 
	 * @return mapa parametrów
	 */
	@Override
	public Map<String, String> reloadConfiguration() {
		synchronized (systemVarsLock) {
			if (systemVars == null) {
				systemVars = loadConfiguration(true);
			} else {
				systemVars = loadConfiguration(false);
			}
			return systemVars;
		}
	}

	/**
	 * Pobranie wartości parametru konfiguracji
	 * 
	 * @param propertyName nazwa parametru
	 * @param defaultValue domyślna wartość parametru (na wypadek gdyby nie był
	 *                     ustawiony)
	 * @return wartość parametru
	 */
	public String getValue(String propertyName, String defaultValue) {
		Map<String, String> properties = loadConfiguration();
		String value = properties.get(propertyName);
		return StringUtils.isNotBlank(value) ? value : defaultValue;
	}

	/**
	 * Właściwa metoda ładowania parametrów systemu
	 * 
	 * @param init czy mapa ma być zainicjalizowana na nowo?
	 * @return mapa parametrów systemu
	 */
	@SuppressWarnings("squid:S2696")
	private Map<String, String> loadConfiguration(boolean init) {
		String configurationDirL = loadConfigurationDir();
		String fileName = configurationDirL + '/' + CONFIGURATION_FILE_NAME;
		if (init) {
			systemVars = loadHashtable(ConfigLoader.class, fileName);
		} else {
			Map<String, String> newVars = loadHashtable(ConfigLoader.class, fileName);
			systemVars.putAll(newVars);
		}
		return systemVars;
	}

	@Override
	public String loadConfigurationDir() {
		return this.configurationDir;
	}

	/**
	 * Metoda czytająca właściwości z pliku. Zawartość pliku powinna byś w UTF-8.
	 * 
	 * @param clazz    dowolna klasa załadowana przez classloader-a, podobnie jak
	 *                 podczas deklaracji obiektu logowania dla "log4j"
	 * @param fileName pełna (z ewentualną ścieżką) nazwa pliku
	 * @return właściwości przeczytane z pliku w postaci Map
	 */
	public static Map<String, String> loadHashtable(Class<?> clazz, String fileName) {
		final String METHODNAME = "loadHashtable";
		logger.logp(Level.INFO, CLASSNAME, METHODNAME, "Loading configuration from file " + fileName);
		Map<String, String> envVars = new HashMap<String, String>();
		BufferReaderClosable b = null;
		try {
			b = getBufferedReader(clazz, fileName);
			BufferedReader d = b.getBuffer();
			if (d == null) {
				return envVars;
			}
			String inputLine;
			while ((inputLine = d.readLine()) != null) {
				if (!inputLine.startsWith("#") && inputLine.length() > 0) {
					int comaPos = inputLine.indexOf('=');
					if (comaPos > 0) {
						String key = inputLine.substring(0, comaPos);
						String value = "";
						if (comaPos < inputLine.length()) {
							value = inputLine.substring(comaPos + 1);
						}
						envVars.put(key, value);
					}
				}
			}
		} catch (IOException e) {
			logger.logp(Level.SEVERE, CLASSNAME, METHODNAME, e.getMessage());
		} finally {
			if (b != null) {
				try {
					b.close();
				} catch (IOException e) {
					/* nie powinno się zdażyć */
				}
			}
		}
		return envVars;
	}

	@SuppressWarnings("squid:S2095")
	private static BufferReaderClosable getBufferedReader(Class<?> clazz, String fileName)
			throws FileNotFoundException {
		BufferedReader d = null;
		URL resource = clazz.getResource(fileName);
		FileInputStream fis = null;
		try {
			if (resource == null) {
				fis = new FileInputStream(fileName);
				d = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
			} else {
				d = new BufferedReader(new InputStreamReader(clazz.getResourceAsStream(fileName), "UTF-8"));
			}
		} catch (UnsupportedEncodingException e) {
			/* nie możliwe */
		}
		return new BufferReaderClosable(fis, d);
	}

	private static class BufferReaderClosable implements Closeable {

		private final FileInputStream fis;
		private final BufferedReader buffer;

		public BufferReaderClosable(FileInputStream fis, BufferedReader d) {
			super();
			this.fis = fis;
			this.buffer = d;
		}

		@Override
		public void close() throws IOException {
			if (buffer != null) {
				buffer.close();
			}
			if (fis != null) {
				fis.close();
			}
		}

		public BufferedReader getBuffer() {
			return buffer;
		}

	}

}
