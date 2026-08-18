package org.princast.oma.servicenowsync.util;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonToCsv {
    
    public static void CambiosToCSV(String entrada, String salida, ServiceNowClient serviceNowClient) throws Exception {
    	ObjectMapper jsonMapper = new ObjectMapper();

        // Leer JSON completo
        Map<String, Object> root = jsonMapper.readValue(new File(entrada), Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) root.get("result");

		for (Map<String, Object> row : data) {
			procesarReferencias(row, serviceNowClient);
        }
        
        CsvMapper csvMapper = new CsvMapper();

		CsvSchema schema = CsvSchema.builder()
		    .addColumn("parent")
		    .addColumn("sys_updated_on")
		    .addColumn("task_effective_number")
		    .addColumn("number")
		    .addColumn("sys_updated_by")
		    .addColumn("opened_by")
		    .addColumn("sys_created_on")
		    .addColumn("requested_by_date")
		    .addColumn("state")
		    .addColumn("sys_created_by")
		    .addColumn("order")
		    .addColumn("closed_at")
		    .addColumn("u_subcategory")
		    .addColumn("cmdb_ci")
		    .addColumn("impact")
		    .addColumn("review_comments")
		    .addColumn("business_service")
		    .addColumn("time_worked")
		    .addColumn("expected_start")
		    .addColumn("opened_at")
		    .addColumn("review_date")
		    .addColumn("business_duration")
		    .addColumn("requested_by")
		    .addColumn("work_end")
		    .addColumn("phase_state")
		    .addColumn("approval_set")
		    .addColumn("work_notes")
		    .addColumn("parent.priority")
		    .addColumn("end_date")
		    .addColumn("short_description")
		    .addColumn("u_department")
		    .addColumn("close_code")
		    .addColumn("work_start")
		    .addColumn("description")
		    .addColumn("calendar_duration")
		    .addColumn("u_no_ci")
		    .addColumn("close_notes")
		    .addColumn("closed_by")
		    .addColumn("urgency")
		    .addColumn("scope")
		    .addColumn("company")
		    .addColumn("reassignment_count")
		    .addColumn("activity_due")
		    .addColumn("start_date")
		    .addColumn("assigned_to")
		    .addColumn("comments")
		    .addColumn("sla_due")
		    .addColumn("sys_mod_count")
		    .addColumn("comments_and_work_notes")
		    .addColumn("due_date")
		    .addColumn("sys_tags")
		    .addColumn("conflict_last_run")
		    .addColumn("correlation_id")
		    .addColumn("risk")
		    .build()
		    .withHeader();

        // Escribir CSV
        csvMapper.writer(schema)
                .writeValue(new File(salida), data);
    }
    
    public static void IncidenciasToCSV(String entrada, String salida, ServiceNowClient serviceNowClient) throws Exception {
    	ObjectMapper jsonMapper = new ObjectMapper();

        // Leer JSON completo
        Map<String, Object> root = jsonMapper.readValue(new File(entrada), Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) root.get("result");

		for (Map<String, Object> row : data) {
			procesarReferencias(row, serviceNowClient);
        }
        
        CsvMapper csvMapper = new CsvMapper();

		CsvSchema schema = CsvSchema.builder()
		    .addColumn("inc_u_department")
		    .addColumn("inc_trigger_rule")
		    .addColumn("inc_description")
		    .addColumn("taskslatable_time_left")
		    .addColumn("taskslatable_sys_created_by")
		    .addColumn("inc_u_impacto_lucia_ccn")
		    .addColumn("inc_u_rpt_resuelto_year_month")
		    .addColumn("inc_rfc")
		    .addColumn("inc_contact_type")
		    .addColumn("inc_u_descripcion_solucion_tipo")
		    .addColumn("inc_company")
		    .addColumn("inc_reopened_by")
		    .addColumn("inc_approval")
		    .addColumn("inc_urgency")
		    .addColumn("taskslatable_sys_tags")
		    .addColumn("inc_caused_by")
		    .addColumn("inc_sys_tags")
		    .addColumn("inc_assigned_to")
		    .addColumn("inc_group_list")
		    .addColumn("inc_u_request_closure")
		    .addColumn("inc_activity_due")
		    .addColumn("inc_escalation")
		    .addColumn("inc_cmdb_ci")
		    .addColumn("taskslatable_business_duration")
		    .addColumn("inc_business_impact")
		    .addColumn("inc_reopen_count")
		    .addColumn("taskslatable_sla")
		    .addColumn("inc_sys_domain_path")
		    .addColumn("inc_u_previous_assignment_group")
		    .addColumn("inc_time_worked")
		    .addColumn("inc_work_notes_list")
		    .addColumn("inc_comments")
		    .addColumn("inc_u_claim_count")
		    .addColumn("inc_u_rpt_tiempo_resol")
		    .addColumn("inc_severity")
		    .addColumn("inc_sys_mod_count")
		    .addColumn("inc_close_code")
		    .addColumn("inc_subcategory")
		    .addColumn("taskslatable_sys_updated_on")
		    .addColumn("inc_overview")
		    .addColumn("inc_reassignment_count")
		    .addColumn("taskslatable_business_percentage")
		    .addColumn("inc_actions_taken")
		    .addColumn("inc_promoted_by")
		    .addColumn("inc_u_rpt_tiempo_resol_inci")
		    .addColumn("taskslatable_has_breached")
		    .addColumn("inc_child_incidents")
		    .addColumn("inc_proposed_by")
		    .addColumn("taskslatable_duration")
		    .addColumn("inc_sys_created_on")
		    .addColumn("inc_origin_id")
		    .addColumn("inc_cause")
		    .addColumn("inc_sys_updated_on")
		    .addColumn("inc_resolved_by")
		    .addColumn("inc_upon_reject")
		    .addColumn("inc_correlation_id")
		    .addColumn("taskslatable_task")
		    .addColumn("inc_lessons_learned")
		    .addColumn("inc_made_sla")
		    .addColumn("inc_u_claimed_by")
		    .addColumn("taskslatable_original_breach_time")
		    .addColumn("inc_upon_approval")
		    .addColumn("inc_business_duration")
		    .addColumn("inc_calendar_duration")
		    .addColumn("inc_route_reason")
		    .addColumn("inc_u_template_id")
		    .addColumn("inc_sla_due")
		    .addColumn("inc_u_type")
		    .addColumn("inc_u_contact")
		    .addColumn("inc_sys_id")
		    .addColumn("inc_correlation_display")
		    .addColumn("inc_major_incident_state")
		    .addColumn("taskslatable_sys_id")
		    .addColumn("inc_additional_assignee_list")
		    .addColumn("taskslatable_percentage")
		    .addColumn("inc_contract")
		    .addColumn("inc_state")
		    .addColumn("taskslatable_u_assignment_group")
		    .addColumn("inc_work_start")
		    .addColumn("inc_approval_history")
		    .addColumn("inc_knowledge")
		    .addColumn("inc_u_rpt_resuelto_mes")
		    .addColumn("inc_due_date")
		    .addColumn("inc_u_claimed")
		    .addColumn("taskslatable_pause_time")
		    .addColumn("inc_universal_request")
		    .addColumn("inc_u_rpt_reuelto_year")
		    .addColumn("inc_close_notes")
		    .addColumn("inc_business_stc")
		    .addColumn("taskslatable_sys_created_on")
		    .addColumn("inc_caller_id")
		    .addColumn("inc_work_notes")
		    .addColumn("taskslatable_start_time")
		    .addColumn("inc_priority")
		    .addColumn("inc_opened_at")
		    .addColumn("inc_assignment_group")
		    .addColumn("inc_parent_incident")
		    .addColumn("inc_parent")
		    .addColumn("inc_skills")
		    .addColumn("taskslatable_planned_end_time")
		    .addColumn("inc_follow_up")
		    .addColumn("inc_task_effective_number")
		    .addColumn("inc_timeline")
		    .addColumn("inc_u_rpt_sespa_resuelto_year_month_texto2")
		    .addColumn("inc_impact")
		    .addColumn("taskslatable_schedule")
		    .addColumn("inc_number")
		    .addColumn("taskslatable_sys_updated_by")
		    .addColumn("taskslatable_business_pause_duration")
		    .addColumn("inc_resolved_at")
		    .addColumn("inc_sys_class_name")
		    .addColumn("inc_service_offering")
		    .addColumn("taskslatable_active")
		    .addColumn("inc_active")
		    .addColumn("inc_incident_state")
		    .addColumn("inc_u_wrong_assignment")
		    .addColumn("taskslatable_end_time")
		    .addColumn("inc_expected_start")
		    .addColumn("inc_sys_domain")
		    .addColumn("inc_sys_created_by")
		    .addColumn("inc_reopened_time")
		    .addColumn("inc_sys_updated_by")
		    .addColumn("inc_location")
		    .addColumn("inc_closed_at")
		    .addColumn("inc_promoted_on")
		    .addColumn("inc_origin_table")
		    .addColumn("inc_u_solucion_tipo")
		    .addColumn("inc_hold_reason")
		    .addColumn("inc_user_input")
		    .addColumn("inc_category")
		    .addColumn("inc_business_service")
		    .addColumn("inc_u_option")
		    .addColumn("inc_watch_list")
		    .addColumn("inc_proposed_on")
		    .addColumn("taskslatable_sys_mod_count")
		    .addColumn("inc_notify")
		    .addColumn("inc_short_description")
		    .addColumn("taskslatable_business_time_left")
		    .addColumn("inc_closed_by")
		    .addColumn("inc_calendar_stc")
		    .addColumn("taskslatable_pause_duration")
		    .addColumn("inc_order")
		    .addColumn("taskslatable_stage")
		    .addColumn("inc_work_end")
		    .addColumn("taskslatable_timezone")
		    .addColumn("inc_u_template_applied")
		    .addColumn("inc_opened_by")
		    .addColumn("inc_u_data_breach")
		    .addColumn("inc_u_vendor")
		    .addColumn("inc_comments_and_work_notes")
		    .addColumn("inc_approval_set")
		    .addColumn("inc_u_pending_request")
		    .addColumn("inc_problem_id")
		    .addColumn("inc_u_security_incident")
		    .build()
		    .withHeader();

        // Escribir CSV
        csvMapper.writer(schema)
                .writeValue(new File(salida), data);
    }
    
    
    public static void SolicitudesToCSV(String entrada, String salida, ServiceNowClient serviceNowClient) throws Exception {
    	ObjectMapper jsonMapper = new ObjectMapper();

        // Leer JSON completo
        Map<String, Object> root = jsonMapper.readValue(new File(entrada), Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) root.get("result");

		for (Map<String, Object> row : data) {
			procesarReferencias(row, serviceNowClient);
        }
        
        CsvMapper csvMapper = new CsvMapper();

		CsvSchema schema = CsvSchema.builder()
		    .addColumn("variables.c8acb6d547da12107b37945e036d43b0")
		    .addColumn("requested_for")
		    .addColumn("sys_updated_on")
		    .addColumn("variables.19477bfa47eb86907b37945e036d4357")
		    .addColumn("variables.4c71a780477a96907b37945e036d4358")
		    .addColumn("task_effective_number")
		    .addColumn("variables.a70ee33e47e786907b37945e036d4354")
		    .addColumn("number")
		    .addColumn("sys_updated_by")
		    .addColumn("u_reopened_date")
		    .addColumn("sys_created_on")
		    .addColumn("state")
		    .addColumn("sys_created_by")
		    .addColumn("order")
		    .addColumn("closed_at")
		    .addColumn("cmdb_ci")
		    .addColumn("variables.988cb6d547da12107b37945e036d4358")
		    .addColumn("impact")
		    .addColumn("priority")
		    .addColumn("time_worked")
		    .addColumn("expected_start")
		    .addColumn("opened_at")
		    .addColumn("business_duration")
		    .addColumn("configuration_item")
		    .addColumn("work_end")
		    .addColumn("work_notes")
		    .addColumn("request")
		    .addColumn("u_department")
		    .addColumn("work_start")
		    .addColumn("assignment_group")
		    .addColumn("u_accept_reopen")
		    .addColumn("description")
		    .addColumn("calendar_duration")
		    .addColumn("close_notes")
		    .addColumn("closed_by")
		    .addColumn("variables.3c9cf2d547da12107b37945e036d4395")
		    .addColumn("variables.d141afcc477a96907b37945e036d43cc")
		    .addColumn("u_previous_assignment_group")
		    .addColumn("urgency")
		    .addColumn("reassignment_count")
		    .addColumn("u_option")
		    .addColumn("variables.2b4c7ed547da12107b37945e036d430d")
		    .addColumn("assigned_to")
		    .addColumn("quantity")
		    .addColumn("comments")
		    .addColumn("due_date")
		    .addColumn("sys_mod_count")
		    .addColumn("comments_and_work_notes")
		    .addColumn("sys_tags")
		    .addColumn("cat_item")
		    .addColumn("stage")
		    .addColumn("u_reopen_count")
		    .addColumn("escalation")
		    .addColumn("estimated_delivery")
		    .addColumn("request.opened_by")
		    .addColumn("variables.18d673ba47eb86907b37945e036d43d2")
		    .build()
		    .withHeader();

        // Escribir CSV
        csvMapper.writer(schema)
                .writeValue(new File(salida), data);
    }
    
    public static void TareasToCSV(String entrada, String salida, ServiceNowClient serviceNowClient) throws Exception {
    	ObjectMapper jsonMapper = new ObjectMapper();

        // Leer JSON completo
        Map<String, Object> root = jsonMapper.readValue(new File(entrada), Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) root.get("result");

		for (Map<String, Object> row : data) {
			procesarReferencias(row, serviceNowClient);
        }
        
        CsvMapper csvMapper = new CsvMapper();

		CsvSchema schema = CsvSchema.builder()
		    .addColumn("sct_number")
		    .addColumn("taskslatable_time_left")
		    .addColumn("sct_assigned_to")
		    .addColumn("sct_opened_at")
		    .addColumn("taskslatable_sys_created_by")
		    .addColumn("sct_due_date")
		    .addColumn("sct_close_notes")
		    .addColumn("taskslatable_pause_time")
		    .addColumn("sct_u_previous_assignment_group")
		    .addColumn("sct_work_start")
		    .addColumn("taskslatable_sys_created_on")
		    .addColumn("sct_impact")
		    .addColumn("taskslatable_start_time")
		    .addColumn("sct_sys_tags")
		    .addColumn("sct_closed_by")
		    .addColumn("taskslatable_planned_end_time")
		    .addColumn("sct_reassignment_count")
		    .addColumn("sct_calendar_stc")
		    .addColumn("taskslatable_business_duration")
		    .addColumn("sct_time_worked")
		    .addColumn("sct_priority")
		    .addColumn("sct_comments")
		    .addColumn("sct_comments_and_work_notes")
		    .addColumn("taskslatable_sys_updated_by")
		    .addColumn("sct_sla_due")
		    .addColumn("taskslatable_business_pause_duration")
		    .addColumn("sct_description")
		    .addColumn("taskslatable_sys_updated_on")
		    .addColumn("sct_task_effective_number")
		    .addColumn("sct_parent")
		    .addColumn("sct_closed_at")
		    .addColumn("taskslatable_end_time")
		    .addColumn("taskslatable_business_percentage")
		    .addColumn("sct_short_description")
		    .addColumn("sct_assignment_group")
		    .addColumn("sct_sys_updated_on")
		    .addColumn("taskslatable_duration")
		    .addColumn("taskslatable_task")
		    .addColumn("sct_request_item")
		    .addColumn("sct_work_notes")
		    .addColumn("taskslatable_sys_mod_count")
		    .addColumn("taskslatable_original_breach_time")
		    .addColumn("sct_sys_created_on")
		    .addColumn("taskslatable_business_time_left")
		    .addColumn("sct_order")
		    .addColumn("taskslatable_pause_duration")
		    .addColumn("taskslatable_stage")
		    .addColumn("sct_expected_start")
		    .addColumn("sct_u_creation_type")
		    .addColumn("sct_activity_due")
		    .addColumn("sct_work_end")
		    .addColumn("sct_cmdb_ci")
		    .addColumn("taskslatable_percentage")
		    .addColumn("taskslatable_u_assignment_group")
		    .addColumn("sct_request")
		    .addColumn("sct_state")
		    .addColumn("sct_opened_by")
		    .addColumn("sct_calendar_duration")
		    .addColumn("sct_business_duration")
		    .addColumn("sct_sys_mod_count")
		    .addColumn("sct_urgency")
		    .build()
		    .withHeader();

        // Escribir CSV
        csvMapper.writer(schema)
                .writeValue(new File(salida), data);
    }
    
    public static void RelacionesToCSV(String entrada, String salida) throws Exception {
    	ObjectMapper jsonMapper = new ObjectMapper();

        // Leer JSON completo
        Map<String, Object> root = jsonMapper.readValue(new File(entrada), Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) root.get("result");

        for (Map<String, Object> row : data) {

            for (Map.Entry<String, Object> entry : row.entrySet()) {

                Object value = entry.getValue();

                if (value instanceof Map) {

                    Map<String, Object> ref = (Map<String, Object>) value;

                    if (ref.containsKey("display_value")) {
                        entry.setValue(ref.get("display_value"));
                    }
                }
            }
        }
        
        CsvMapper csvMapper = new CsvMapper();

		CsvSchema schema = CsvSchema.builder()
		    .addColumn("parent")
		    .addColumn("child.closed_at")
		    .addColumn("parent.closed_at")
		    .addColumn("child")
		    .build()
		    .withHeader();

        // Escribir CSV
        csvMapper.writer(schema)
                .writeValue(new File(salida), data);
    }
    
    public static void IncidenciasTMPToCSV(String entrada, String salida, ServiceNowClient serviceNowClient) throws Exception {
    	ObjectMapper jsonMapper = new ObjectMapper();

        // Leer JSON completo
        Map<String, Object> root = jsonMapper.readValue(new File(entrada), Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) root.get("result");

		for (Map<String, Object> row : data) {
			procesarReferencias(row, serviceNowClient);
        }
        
        CsvMapper csvMapper = new CsvMapper();

		CsvSchema schema = CsvSchema.builder()
		    .addColumn("inc_subcategory")
		    .addColumn("inc_number")
		    .build()
		    .withHeader();

        // Escribir CSV
        csvMapper.writer(schema)
                .writeValue(new File(salida), data);
    }
    
    public static void FactoriaToCSV(String entrada, String salida, ServiceNowClient serviceNowClient) throws Exception {
    	ObjectMapper jsonMapper = new ObjectMapper();

        // Leer JSON completo
        Map<String, Object> root = jsonMapper.readValue(new File(entrada), Map.class);

        List<Map<String, Object>> data = (List<Map<String, Object>>) root.get("result");

		for (Map<String, Object> row : data) {
			procesarReferencias(row, serviceNowClient);


//			String valorGrupo1 = getPrimerValor(row, "variables.18d673ba47eb86907b37945e036d43d2", "campo_B", "campo_C");
//			
//			String valorGrupo2 = getPrimerValor(row, "variables.e02f86f14795b6107b37945e036d4386", "campo_E", "campo_F");
//			
//			row.put("campo_final_1", valorGrupo1);
//		    row.put("campo_final_2", valorGrupo2);

        }
        
        CsvMapper csvMapper = new CsvMapper();

		CsvSchema schema = CsvSchema.builder()
		    .addColumn("number")
		    .addColumn("closed_at")
		    .addColumn("assignment_group")
		    .addColumn("variables.e02f86f14795b6107b37945e036d4386")
		    .addColumn("variables.18d673ba47eb86907b37945e036d43d2")
		    .addColumn("variables.fb8fa5cb87b4fed055e0dd383cbb3567")
		    .addColumn("variables.ff7b06234709fe107b37945e036d43a3")
		    .addColumn("variables.7cea8e6b47c5fe107b37945e036d430c")
		    .addColumn("variables.29fe2ee487017a1055e0dd383cbb35d1")
		    .build()
		    .withHeader();

        // Escribir CSV
        csvMapper.writer(schema)
                .writeValue(new File(salida), data);
    }

	private static Map<String, String> cache = new HashMap<>();
	
	public static String resolverReferencia(Map<String, Object> ref, ServiceNowClient serviceNowClient) throws Exception {
	
	    String url = (String) ref.get("link");
	
	    try {
			if (cache.containsKey(url)) {
			    return cache.get(url);
			}
		
	
	    Map<String, Object> response = serviceNowClient.getDataFromUrl(url);
	
	    Map<String, Object> result =
	            (Map<String, Object>) response.get("result");
	
	    String name = (String) result.get("name");
	
	    cache.put(url, name);
	
	    return name;
	    } catch (Exception e) {

	    	String display = (String) ref.get("display_value");

	        log.warn("No se pudo resolver link: {}", url);
	        log.warn("Usando display_value: {}", display);
	
	        cache.put(url, display);
	
	        return display;

	    }
	    
	}
	
	public static void procesarReferencias(Map<String, Object> row, ServiceNowClient serviceNowClient) throws Exception {

	    for (Map.Entry<String, Object> entry : row.entrySet()) {

	        Object value = entry.getValue();

	        // ✅ Detectar referencia ServiceNow
	        if (value instanceof Map) {

	            Map<String, Object> ref = (Map<String, Object>) value;

	            if (ref.containsKey("link")) {
	            	
	            	String url = (String) ref.get("link");
	            	

	            	try {
		                log.debug("Resolviendo campo: {} -> {}", entry.getKey(), url);
		
		                String resolved = resolverReferencia(ref, serviceNowClient);
		
		                entry.setValue(resolved);
		
		            } catch (Exception e) {

		            	log.error("Error resolviendo campo {} (url: {})", entry.getKey(), url, e);

		            	entry.setValue(ref.get("display_value"));

		            }


	                String resolved = resolverReferencia(ref, serviceNowClient);

	                entry.setValue(resolved);
	            }
	        }
	    }
	}

	public static String getPrimerValor(Map<String, Object> row, String... campos) {
	
	    for (String campo : campos) {
	
	        Object value = row.get(campo);
	
	        if (value != null) {
	            String str = value.toString().trim();
	
	            if (!str.isEmpty()) {
	                return str;
	            }
	        }
	    }
	
	    return null;
	}
}