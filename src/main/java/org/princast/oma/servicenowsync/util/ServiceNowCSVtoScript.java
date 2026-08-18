package org.princast.oma.servicenowsync.util;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.princast.oma.servicenowsync.processor.ServiceNowProcessor;

import com.opencsv.CSVReader;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ServiceNowCSVtoScript {
	

	private static final DateTimeFormatter INPUTDATE =
	        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
	
	private static final DateTimeFormatter OUTPUTDATE =
	        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	public void csvToScript(String csvFile, String outputSql, String tabla) {
		
		log.info("Cargando datos para la tabla: " + tabla);
		
		int numeroLinea = 1;
		
		try  {
			CSVReader br = new CSVReader(new FileReader(csvFile));
			BufferedWriter bw = new BufferedWriter(new FileWriter(outputSql));
			
			String[] line;
			
			line = br.readNext();
			if(line == null) {
				bw.close();
				br.close();
				throw new IllegalStateException("CSV vacío");
			}
			
			Properties mapping = null;
			
			switch (tabla) {
			case "MAEINCIDENCIA_SNOW":
				mapping = loadProperties("API/incidencias.properties");
				break;
				
			case "TMP_INCIDENCIA_SLA":
				mapping = loadProperties("API/tmp_incidencias.properties");
				break;
				
			case "MAECAMBIO_SNOW":
				mapping = loadProperties("API/cambios.properties");
				break;
	
			case "MAESOLICITUD_SNOW":
				mapping = loadProperties("API/solicitudes.properties");
				break;
			
			case "MAETAREASOLICITUD_SNOW": 
				mapping = loadProperties("API/tareas.properties");
				break;
			
			case "MAERELACION_SNOW":
				mapping = loadProperties("API/relaciones.properties");
				break;
				
			default:
				mapping = loadProperties("API/tmp_solicitud.properties");
				break;
			}


			List<Integer> indicesColumna = new ArrayList<>();
			List<String> columnasSQL = new ArrayList<>();
			
			for (int i = 0; i < line.length; i++) {
			    String cabecera = line[i].trim().replace("\"", "");
			    
			    if (mapping.keySet().contains(cabecera)) {
			    	indicesColumna.add(i);
			    	columnasSQL.add(mapping.getProperty(cabecera));
			    }
			}
			
			log.debug("Columnas SQL: {}", columnasSQL);
			
			String baseInsert = "INSERT INTO "+ tabla + "(" +
			        String.join(", ", columnasSQL) +
			        ") VALUES ";
			
			String[] linea;
			
			while ((linea = br.readNext()) != null) {
			
				numeroLinea++;
				
			    StringBuilder rowValues = new StringBuilder("(");
			    
			    for (int i = 0; i < indicesColumna.size(); i++) {
			        int index = indicesColumna.get(i);

			        String nombreColumna = columnasSQL.get(i);
			        String valor = linea[index];
			        
			         if (nombreColumna.startsWith("FE_")) {

			             if (!esFechaValida(valor)) {
			                 rowValues.append("NULL");
			             } else {
			                 rowValues.append(formatValue(valor));
			             }

			         } else if (nombreColumna.startsWith("TE_")) {
							
			        	 if (valor.equals("verdadero")) {
			        		 valor = "S";
			        	 }
				         valor = limpiarTexto(valor);
				         rowValues.append(formatValue(valor));
					
					 } else if (nombreColumna.startsWith("FL_")) {
							
						 if(valor.equals("falso")) {
							 valor = "N";
						 } else if(valor.equals("verdadero")) {
							 valor = "S";
						 }
					     rowValues.append(formatValue(valor));
						
					 } else if (nombreColumna.startsWith("NU_")) {
						 
						 try {
							 Double.parseDouble(valor);
							 
						 } catch(Exception e) {
							 
							 valor = String.valueOf(duracionASegundos(valor));
						 }
						 
						 rowValues.append(formatValue(valor));
						 
					 } else {
			            rowValues.append(formatValue(valor));
			         } 
			
			        if (i < indicesColumna.size() - 1) {
			            rowValues.append(", ");
			        }
			    }
			    rowValues.append(");");

			    log.debug("{}{}", baseInsert, rowValues);

			    bw.write(baseInsert + rowValues);
                bw.newLine();
			}
			
			br.close();
			bw.flush();
			bw.close();
			
		} catch(Exception e){
			log.error("error en fila: " + numeroLinea);
			log.error(e.toString());
			
		}
		
		log.info("Cargados los datos para la tabla: " + tabla);
	}
	
	
	public static Properties loadProperties(String file) {
		Properties props = new Properties();
		try {
			InputStream in = new FileInputStream(file);
			props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
		} catch (Exception e) {
			log.error("Error cargando mapping: " + file + e);
		}
		return props;
	}
	

	private static String formatValue(String value) {
	    if (value == null || value.isBlank()) {
	        return "null";
	    }
	
	    value = value.trim();
		value = value.replace("\u00A0", " ")
		                 .replace("\u200B", "")
		                 .replace("\t", " ");

		if (value.startsWith("\"") && value.endsWith("\"")) {
		        value = value.substring(1, value.length() - 1);
		    }

		if (value.length() > 4000) {
		    value = value.substring(0, 4000);
		}

		try {
		        LocalDateTime date = LocalDateTime.parse(value, INPUTDATE);
		        return "TO_DATE('" + date.format(OUTPUTDATE) + "', 'YYYY-MM-DD HH24:mi:ss')";
		    } catch (DateTimeParseException ignored) {
		    	
		    }
		
		value = value.replaceAll("\\r\\n|\\r|\\n", " ");
	
	    if (value.matches("-?\\d+(\\.\\d+)?")) {
	        return value;
	    }
	    
	    return "'" + value.replace("'", "''") + "'";
	}
	

	private static boolean esFechaValida(String fecha) {
	
	    if (fecha == null || fecha.trim().isEmpty()) {
	        return false;
	    }
	
	    try {
	        LocalDateTime.parse(fecha.trim(), INPUTDATE);
	        return true;
	    } catch (DateTimeParseException e) {
	        return false;
	    }
	}
	

	private static String limpiarTexto(String texto) {
	
	    if (texto == null) {
	        return null;
	    }
	    

		if (texto.length() > 255) {
		        texto = texto.substring(0, 255);
		    }

	
	    return texto
	            .replace("\t", " ")     // tabuladores
	            .replace("\r", " ")     // retorno de carro
	            .replace("\n", " ")     // salto de línea
	            .replaceAll("\\s+", " ") // múltiples espacios
	            .trim();
	}

	private static Long duracionASegundos(String texto) {

	    long segundos = 0;

	    Pattern pDias = Pattern.compile("(\\d+)\\s*D[ií]as?", Pattern.CASE_INSENSITIVE);
	    Pattern pHoras = Pattern.compile("(\\d+)\\s*Horas?", Pattern.CASE_INSENSITIVE);
	    Pattern pMinutos = Pattern.compile("(\\d+)\\s*minutos?", Pattern.CASE_INSENSITIVE);

	    Matcher mDias = pDias.matcher(texto);
	    Matcher mHoras = pHoras.matcher(texto);
	    Matcher mMinutos = pMinutos.matcher(texto);

	    if (mDias.find()) {
	        segundos += Long.parseLong(mDias.group(1)) * 86400;
	    }

	    if (mHoras.find()) {
	        segundos += Long.parseLong(mHoras.group(1)) * 3600;
	    }

	    if (mMinutos.find()) {
	        segundos += Long.parseLong(mMinutos.group(1)) * 60;
	    }

	    return segundos;
	}

}
