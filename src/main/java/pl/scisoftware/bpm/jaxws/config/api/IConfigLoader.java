package pl.scisoftware.bpm.jaxws.config.api;

import java.util.Map;

public interface IConfigLoader {

	/**
	 * Ładowania parametrów konfiguracyjnych systemu.
	 * 
	 * @return mapa parametrów
	 */
	Map<String, String> loadConfiguration();

	/**
	 * Ładowania/przeładowanie parametrów konfiguracyjnych systemu.
	 * 
	 * @return mapa parametrów
	 */
	Map<String, String> reloadConfiguration();

	/**
	 * Ładowanie ścieżki konfiguracyjnej systemu
	 * 
	 * @return ścieżka katalogu, w którym znajduje się konfiguracja.
	 */
	String loadConfigurationDir();

	/**
	 * Pobranie wartości parametru konfiguracji
	 * 
	 * @param propertyName nazwa parametru
	 * @param defaultValue domyślna wartość parametru (na wypadek gdyby nie był
	 *                     ustawiony)
	 * @return wartość parametru
	 */
	public String getValue(String propertyName, String defaultValue);

}