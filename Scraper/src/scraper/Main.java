package scraper;

import java.io.*;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import sun.misc.BASE64Decoder;

/**
 *
 * @author Rodrigo
 */
public class Main {

	static public void generarCVSEntrenamiento() throws IOException {
		Map<String,Boolean> tablaUrls = LectorCSV.run();
		List<Ejemplo> ejemplos = new LinkedList();
		File folder = new File("C:\\Fing\\ProyGrado\\entrenar\\");
		File[] listOfFiles = folder.listFiles();
		for (File file : listOfFiles) {
			if (file.isFile()) {
				System.out.println(file.getName());
				BASE64Decoder decoder = new BASE64Decoder();
				byte[] decodedBytes = decoder.decodeBuffer(file.getName());
				String url = new String(decodedBytes);
				System.out.println(url);

				String html = readFile(file, "UTF-8");

				if (ProcesadorHTML.obtenerCharset(html).equals("iso-8859-1") || ProcesadorHTML.obtenerCharset(html).equals("ISO-8859-1")) {

					html = readFile(file, "ISO-8859-1");

				} else if (ProcesadorHTML.obtenerCharset(html).equals("Windows-1252") || ProcesadorHTML.obtenerCharset(html).equals("windows-1252")) {
					html = readFile(file, "Windows-1252");
				}
				ProcesadorHTML procHTML = new ProcesadorHTML(html, url);
				Ejemplo ej = new Ejemplo(procHTML, tablaUrls.get(file.getName()));
				ejemplos.add(ej);
			}
		}
		Ejemplo.guardarCSV("C:\\Fing\\ProyGrado\\csv\\ejemplos.csv", ejemplos);
	}

	static public String readFile(String file, String encoding) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		while (!reader.ready());
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		reader.close();
		return stringBuilder.toString();
	}

	static public String readFile(File file, String encoding) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
		while (!reader.ready());
		String line = null;
		StringBuilder stringBuilder = new StringBuilder();
		String ls = System.getProperty("line.separator");
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
			stringBuilder.append(ls);
		}
		reader.close();
		return stringBuilder.toString();
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args) {
		try {

			Configuracion config = new Configuracion();
			final int maxIterFreeling = 25;

			// Creo una lista de ejemplos vacia, para entrenar
			
			System.out.println("toy aca!!!!!!!");
			System.out.println("Entrenar clasificador?s/n");
			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			String entrenarClasificador = in.readLine();			
			if (entrenarClasificador.equals("s")) {
				Main.generarCVSEntrenamiento();
				Clasificador clasif = new Clasificador("C:\\Fing\\ProyGrado\\csv\\ejemplos.csv");
				clasif.crearModelo();
			}
	
			/**
			 * *******************************************************************
			 * Cambie la carpeta para una de pruebas con archivos bajados a mano
			 * *
			 * *******************************************************************
			 */
			File folder = new File("C:\\Fing\\ProyGrado\\pruebas");

			File[] listOfMediosPrensa = folder.listFiles();
			for (File medioPrensa : listOfMediosPrensa) {
				File[] listOfFolders = medioPrensa.listFiles();
				String medioActual = medioPrensa.getName();
				String nomArchivo = "C:\\Fing\\ProyGrado\\htmlprocesado\\" + medioActual + ".xml";
				String nomArchivoNoti = "C:\\Fing\\ProyGrado\\htmlprocesado\\" + medioActual + "Noticias.xml";
				Writer bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nomArchivo)));
				Writer bwNoticias = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(nomArchivoNoti)));
				bw.append("<add>");
				bwNoticias.append("<add>");
				ProcesadorPaginas proc = new ProcesadorPaginas(config, medioActual);
				int i = 0;
				for (File carpeta_fecha : listOfFolders) {
					File[] listOfFiles = carpeta_fecha.listFiles();
					for (File file : listOfFiles) {
						if (file.isFile()) {
							System.out.println(file.getName());
							BASE64Decoder decoder = new BASE64Decoder();
							byte[] decodedBytes = decoder.decodeBuffer(file.getName());
							String url = new String(decodedBytes);
							System.out.println(url);

							String html = readFile(file, "UTF-8");

							if (ProcesadorHTML.obtenerCharset(html).equals("iso-8859-1") || ProcesadorHTML.obtenerCharset(html).equals("ISO-8859-1")) {

								html = readFile(file, "ISO-8859-1");

							} else if (ProcesadorHTML.obtenerCharset(html).equals("Windows-1252") || ProcesadorHTML.obtenerCharset(html).equals("windows-1252")) {
								html = readFile(file, "Windows-1252");
							}
							
							ProcesadorHTML procHTML = new ProcesadorHTML(html, url);
							

							
							String xmlNoticiaTmp = proc.procesar(procHTML);
							
							if (!xmlNoticiaTmp.isEmpty()) {
								String xmlNoticia = "<doc>\r\n";
								xmlNoticia += xmlNoticiaTmp;
								xmlNoticia += "</doc>\r\n";
								bwNoticias.append(xmlNoticia);
								bwNoticias.flush();
							}
							
							i++;


						}
						if (i >= maxIterFreeling) {
							String xml = proc.taggear();
							bw.append(xml);
							bw.flush();
							i = 0;
						}
					}

					if (i >= maxIterFreeling) {
						String xml = proc.taggear();
						bw.append(xml);
						bw.flush();
						i = 0;
					}

				}
				if (i != 0) {
					String xml = proc.taggear();
					bw.append(xml);
					bw.flush();
				}
				bw.append("</add>");
				bwNoticias.append("</add>");
				bw.flush();
				bwNoticias.flush();
				bw.close();
				bwNoticias.close();
			}
		} catch (Exception e) {
			System.out.println(e);
		}

	}
}
