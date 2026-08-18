package org.princast.oma.servicenowsync.util;

import java.io.FileOutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.princast.oma.servicenowsync.config.ServiceNowProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ServiceNowClient {

    private final String user;
    private final String password;

    private static final ObjectMapper mapper = new ObjectMapper();
    private static final HttpClient client = HttpClient.newHttpClient();

    public ServiceNowClient(ServiceNowProperties properties) {
        this.user = properties.getCredentials().getUser();
        this.password = properties.getCredentials().getPassword();
    }

    public void descargarCSV(String reportId, String rutaDestino) throws Exception {
 
        // 1. Desactivar validación SSL
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
 
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 
        // 2. Construir URL
        String url = "https://itsmasturias.service-now.com/sys_report_template.do?CSV&jvar_report_id=" + reportId;
 
        log.info("Descargando informe con ID: {}", reportId);
        // 3. Configurar RestTemplate con autenticación
        RestTemplate restTemplate = new RestTemplate();
 
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setAccept(MediaType.parseMediaTypes("text/xlsx"));
 
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
 
        // 4. Llamada y obtención como stream (mejor para ficheros)
        ResponseEntity<byte[]> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                byte[].class
        );
 
        // 5. Crear carpeta si no existe
        Path path = Path.of(rutaDestino);
        Files.createDirectories(path.getParent());
 
        // 6. Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
            fos.write(response.getBody());
        }
        
        log.info("Descargado y guardado en: {}", rutaDestino);
    }
    
    public void descargarJSONCambios(String rutaDestino, String fechaDesde, String fechaHasta) throws Exception {
    	 
        // Desactivar validación SSL
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
 
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 
        String url = "https://itsmasturias.service-now.com/api/now/table/change_request?sysparm_limit=1000&sysparm_query=category=apps^u_subcategory=integ_deploy^ORu_subcategory=transport^state=3"
        		+ "^closed_atBETWEEN" + fechaDesde + "@" + fechaHasta
        		+ "&sysparm_fields=number,short_description,state,start_date,end_date,assigned_to,opened_at,opened_by,sys_mod_count,sys_updated_on,sys_updated_by,cmdb_ci,closed_at,closed_by,"
        		+ "comments_and_work_notes,conflict_last_run,sys_created_on,sys_created_by,close_code,u_department,description,calendar_duration,business_duration,company,phase_state,sys_tags,"
        		+ "approval_set,work_end,work_start,review_date,due_date,sla_due,correlation_id,impact,expected_start,u_no_ci,close_notes,work_notes,reassignment_count,task_effective_number,"
        		+ "comments,review_comments,order,parent,parent.priority,risk,business_service,requested_by,requested_by_date,u_subcategory,time_worked,urgency,activity_due,scope&sysparm_display_value=true";
                		
        log.info("Descargando informe de cambios");
        
        // Configurar RestTemplate con autenticación
        RestTemplate restTemplate = new RestTemplate();
 
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
 
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
 
        // Llamada y obtención como stream (mejor para ficheros)
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        log.debug(response.getBody());
 
        // Crear carpeta si no existe
        Path path = Path.of(rutaDestino);
        Files.createDirectories(path.getParent());
 
        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
        	String json = response.getBody();
        	Files.writeString(path, json, StandardCharsets.UTF_8);
        }
        
        log.info("Descargado y guardado en: {}", rutaDestino);
    }
    
    public void descargarJSONIncidencias(String rutaDestino, String fechaDesde, String fechaHasta) throws Exception {
   	 
        // Desactivar validación SSL
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
 
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 
        String url =
        		"https://itsmasturias.service-now.com/api/now/table/incident_sla" +
        		"?sysparm_limit=1000" +
        		"&sysparm_display_value=true" +
        		"&sysparm_query=taskslatable_u_assignment_group.nameSTARTSWITHMNTO_L" +
        		"^inc_state=7" +
        		"^ORinc_state=6" +
        		"^inc_closed_atBETWEEN"
        		+ fechaDesde + "@" + fechaHasta;
        
        log.info("Descargando informe de incidencias");
        
        // Configurar RestTemplate con autenticación
        RestTemplate restTemplate = new RestTemplate();
 
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
 
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
 
        // Llamada y obtención como stream (mejor para ficheros)
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        log.debug(response.getBody());
 
        // Crear carpeta si no existe
        Path path = Path.of(rutaDestino);
        Files.createDirectories(path.getParent());
 
        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
        	String json = response.getBody();
        	Files.writeString(path, json, StandardCharsets.UTF_8);
        }
        
        log.info("Descargado y guardado en: {}", rutaDestino);
    }
    
    public void descargarJSONSolicitudes(String rutaDestino, String fechaDesde, String fechaHasta) throws Exception {
      	 
        // Desactivar validación SSL
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
 
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 
        String url = "https://itsmasturias.service-now.com/api/now/table/sc_req_item?sysparm_limit=1000&sysparm_display_value=true&sysparm_query=cat_item=9d5ddfab4732c2107b37945e036d4320^ORcat_item=10ebbdd487ffea9055e0dd383cbb357e^ORcat_item=4b34c15e8717ae1055e0dd383cbb354e^stage=complete^closed_atBETWEEN"
        		+ fechaDesde + "@" + fechaHasta 
        		+ "&sysparm_fields=number,cat_item,stage,request,requested_for,request.opened_by,due_date,quantity,assignment_group,opened_at,u_accept_reopen,sys_mod_count,sys_updated_on,sys_updated_by,assigned_to,cmdb_ci,closed_at,closed_by,comments_and_work_notes,sys_created_on,sys_created_by,u_department,description,calendar_duration,business_duration,configuration_item,estimated_delivery,escalation,state,sys_tags,work_end,u_previous_assignment_group,impact,expected_start,work_start,close_notes,work_notes,reassignment_count,task_effective_number,comments,u_option,order,priority,u_reopen_count,u_reopened_date,time_worked,urgency,variables.a70ee33e47e786907b37945e036d4354,variables.18d673ba47eb86907b37945e036d43d2,variables.19477bfa47eb86907b37945e036d4357,variables.d141afcc477a96907b37945e036d43cc,variables.2b4c7ed547da12107b37945e036d430d,variables.4c71a780477a96907b37945e036d4358,variables.988cb6d547da12107b37945e036d4358,variables.3c9cf2d547da12107b37945e036d4395,variables.c8acb6d547da12107b37945e036d43b0";
        
        log.info("Descargando informe de solicitudes");
        
        // Configurar RestTemplate con autenticación
        RestTemplate restTemplate = new RestTemplate();
 
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
 
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
 
        // Llamada y obtención como stream (mejor para ficheros)
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        log.debug(response.getBody());
 
        // Crear carpeta si no existe
        Path path = Path.of(rutaDestino);
        Files.createDirectories(path.getParent());
 
        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
        	String json = response.getBody();
        	Files.writeString(path, json, StandardCharsets.UTF_8);
        }
        
        log.info("Descargado y guardado en: {}", rutaDestino);
    }
    
    public void descargarJSONTareas(String rutaDestino, String fechaDesde, String fechaHasta) throws Exception {
     	 
        // Desactivar validación SSL
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
 
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 
        String url = "https://itsmasturias.service-now.com/api/now/table/sc_task_sla?sysparm_limit=1000&sysparm_display_value="
        		+ "true&sysparm_query=taskslatable_u_assignment_group.nameSTARTSWITHMNTO_L^sct_cat_item=9d5ddfab4732c2107b37945e"
        		+ "036d4320^ORsct_cat_item=10ebbdd487ffea9055e0dd383cbb357e^ORsct_cat_item=4b34c15e8717ae1055e0dd383cbb354e^sct_"
        		+ "state=3^ORsct_state=7^ORsct_state=4^sct_closed_atBETWEEN"
        		+ fechaDesde + "@" + fechaHasta + "&sysparm_fields=sct_number,sct_short_description,taskslatable_stage,sct_opened_at,"
        		+ "sct_opened_by,sct_sys_mod_count,taskslatable_sys_mod_count,sct_sys_updated_on,taskslatable_sys_updated_on,taskslatable_sys_updated_by,sct_assigned_to,sct_cmdb_ci,sct_closed_at,sct_closed_by,sct_comments_and_work_notes,sct_sys_created_on,taskslatable_sys_created_on,taskslatable_sys_created_by,sct_description,sct_calendar_duration,taskslatable_pause_duration,taskslatable_business_pause_duration,sct_business_duration,taskslatable_business_duration,taskslatable_duration,sct_request_item,sct_state,sct_sys_tags,sct_due_date,sct_sla_due,sct_work_end,sct_u_previous_assignment_group,sct_assignment_group,taskslatable_end_time,taskslatable_planned_end_time,taskslatable_start_time,sct_impact,sct_expected_start,sct_work_start,sct_close_notes,sct_work_notes,sct_reassignment_count,sct_task_effective_number,sct_comments,sct_order,taskslatable_business_percentage,taskslatable_percentage,sct_parent,sct_priority,taskslatable_u_assignment_group,sct_request,taskslatable_task,taskslatable_business_time_left,taskslatable_pause_time,sct_calendar_stc,taskslatable_original_breach_time,taskslatable_time_left,sct_time_worked,sct_u_creation_type,sct_urgency,sct_activity_due";
        
        log.info("Descargando informe de tareas");
        
        // Configurar RestTemplate con autenticación
        RestTemplate restTemplate = new RestTemplate();
 
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
 
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
 
        // Llamada y obtención como stream (mejor para ficheros)
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        log.debug(response.getBody());
 
        // Crear carpeta si no existe
        Path path = Path.of(rutaDestino);
        Files.createDirectories(path.getParent());
 
        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
        	String json = response.getBody();
        	Files.writeString(path, json, StandardCharsets.UTF_8);
        }
        
        log.info("Descargado y guardado en: {}", rutaDestino);
    }
    
    public void descargarJSONRelaciones(String rutaDestino, String fechaDesde, String fechaHasta) throws Exception {
    	 
        // Desactivar validación SSL
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
 
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 
        String url = "https://itsmasturias.service-now.com/api/now/table/task_rel_task?sysparm_limit=10000&sysparm_display_value=true&sysparm_query=child.closed_atISNOTEMPTY^parent.closed_atISNOTEMPTY^parent.closed_atBETWEEN"
        		+ fechaDesde + "@" + fechaHasta 
        		+ "&sysparm_fields=parent,child,parent.closed_at,child.closed_at";
        		
        log.info("Descargando informe de relaciones");
        
        // Configurar RestTemplate con autenticación
        RestTemplate restTemplate = new RestTemplate();
 
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
 
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
 
        // Llamada y obtención como stream (mejor para ficheros)
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        log.debug(response.getBody());
 
        // Crear carpeta si no existe
        Path path = Path.of(rutaDestino);
        Files.createDirectories(path.getParent());
 
        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
        	String json = response.getBody();
        	Files.writeString(path, json, StandardCharsets.UTF_8);
        }
        
        log.info("Descargado y guardado en: {}", rutaDestino);
    }
    
    
    public void descargarJSONIncidenciasTMP(String rutaDestino, String fechaDesde, String fechaHasta) throws Exception {
   	 
        // Desactivar validación SSL
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
 
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 
        String url = "https://itsmasturias.service-now.com/api/now/table/incident_sla?sysparm_limit=99999&sysparm_display_value=true&sysparm_query="
        		+ "taskslatable_u_assignment_groupSTARTSWITHMNTO_L&sysparm_fields=inc_number,inc_subcategory";
        		
        log.info("Descargando informe de incidenciasTMP");
        
        // Configurar RestTemplate con autenticación
        RestTemplate restTemplate = new RestTemplate();
 
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
 
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
 
        // Llamada y obtención como stream (mejor para ficheros)
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        log.debug(response.getBody());
 
        // Crear carpeta si no existe
        Path path = Path.of(rutaDestino);
        Files.createDirectories(path.getParent());
 
        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
        	String json = response.getBody();
        	Files.writeString(path, json, StandardCharsets.UTF_8);
        }
        
        log.info("Descargado y guardado en: {}", rutaDestino);
    }
    
    public void descargarJSONFactoria(String rutaDestino, String fechaDesde, String fechaHasta) throws Exception {
      	 
        // Desactivar validación SSL
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
 
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 
        HostnameVerifier allHostsValid = (hostname, session) -> true;
        HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 
        String url = "https://itsmasturias.service-now.com/api/now/table/sc_req_item?sysparm_limit=1000&sysparm_display_value="
        		+ "true&sysparm_query=variables.c8acb6d547da12107b37945e036d43b0=Yes^cat_item=9d5ddfab4732c2107b37945e036d4320^"
        		+ "ORcat_item=4b34c15e8717ae1055e0dd383cbb354e^ORcat_item=10ebbdd487ffea9055e0dd383cbb357e&sysparm_fields=number,"
        		+ "assignment_group,closed_at,variables.18d673ba47eb86907b37945e036d43d2,variables.ff7b06234709fe107b37945e036d43a3,variables.fb8fa5cb87b4fed055e0dd383cbb3567,"
        		+ "variables.e02f86f14795b6107b37945e036d4386,variables.7cea8e6b47c5fe107b37945e036d430c,variables.29fe2ee487017a1055e0dd383cbb35d1";
        		
        log.info("Descargando informe de horas de factoría");
        
        // Configurar RestTemplate con autenticación
        RestTemplate restTemplate = new RestTemplate();
 
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(user, password);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));
 
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);
 
        // Llamada y obtención como stream (mejor para ficheros)
        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                requestEntity,
                String.class
        );
        
        log.debug(response.getBody());
 
        // Crear carpeta si no existe
        Path path = Path.of(rutaDestino);
        Files.createDirectories(path.getParent());
 
        // Guardar archivo
        try (FileOutputStream fos = new FileOutputStream(path.toFile())) {
        	String json = response.getBody();
        	Files.writeString(path, json, StandardCharsets.UTF_8);
        }
        
        log.info("Descargado y guardado en: {}", rutaDestino);
    }

	public Map<String, Object> getDataFromUrl(String url) throws Exception {

	        // Crear header de autenticación (Basic Auth)
	        String auth = user + ":" + password;
	        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
	
	        // Construir request
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(new URI(url))
	                .header("Authorization", "Basic " + encodedAuth)
	                .header("Accept", "application/json")
	                .GET()
	                .build();
	
	        // Ejecutar llamada
	        HttpResponse<String> response =
	                client.send(request, HttpResponse.BodyHandlers.ofString());
	        log.debug(response.body());
	        
	        // Validar respuesta HTTP
	        if (response.statusCode() != 200) {
	            throw new RuntimeException(
	                    "Error en llamada a ServiceNow: HTTP " + response.statusCode()
	            );
	        }

	        return mapper.readValue(response.body(), Map.class);
	    }
	
	
}