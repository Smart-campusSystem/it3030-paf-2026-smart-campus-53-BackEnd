package com.smart_campus_system.demo;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TicketApiTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@Test
	void createTicket_withoutAuth_returnsCreated() throws Exception {
		mockMvc.perform(multipart("/api/tickets")
						.param("category", "Equipment malfunction")
						.param("description", "Projector not powering on")
						.param("priority", "HIGH")
						.param("contactName", "Alex Reporter")
						.param("contactEmail", "alex@example.com")
						.param("contactPhone", "555-0100"))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.status").value("OPEN"))
				.andExpect(jsonPath("$.priority").value("HIGH"));
	}

	@Test
	void getTicket_public() throws Exception {
		MvcResult created = mockMvc.perform(multipart("/api/tickets")
						.param("category", "Room issue")
						.param("description", "AC broken")
						.param("priority", "MEDIUM")
						.param("contactName", "Sam")
						.param("contactEmail", "sam@example.com")
						.param("contactPhone", "555-0200"))
				.andExpect(status().isCreated())
				.andReturn();
		long id = readId(created);
		mockMvc.perform(get("/api/tickets/" + id))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.category").value("Room issue"));
	}

	@Test
	void updateStatus_asTechnician() throws Exception {
		MvcResult created = mockMvc.perform(multipart("/api/tickets")
						.param("category", "Equipment malfunction")
						.param("description", "Broken cable")
						.param("priority", "LOW")
						.param("contactName", "Riley")
						.param("contactEmail", "riley@example.com")
						.param("contactPhone", "555-0300"))
				.andExpect(status().isCreated())
				.andReturn();
		long id = readId(created);
		mockMvc.perform(put("/api/tickets/" + id + "/status")
						.with(user("tech1").roles("TECHNICIAN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"status\":\"IN_PROGRESS\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("IN_PROGRESS"));
	}

	@Test
	void addComment_asUser() throws Exception {
		MvcResult created = mockMvc.perform(multipart("/api/tickets")
						.param("category", "Other")
						.param("description", "Need help")
						.param("priority", "MEDIUM")
						.param("contactName", "Student")
						.param("contactEmail", "student@example.com")
						.param("contactPhone", "555-0400"))
				.andExpect(status().isCreated())
				.andReturn();
		long id = readId(created);
		mockMvc.perform(post("/api/tickets/" + id + "/comments")
						.with(user("student1").roles("USER"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"text\":\"Additional details\"}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.comments[0].text").value("Additional details"));
	}

	@Test
	void assignTechnician_forbiddenForNonAdmin() throws Exception {
		MvcResult created = mockMvc.perform(multipart("/api/tickets")
						.param("category", "Other")
						.param("description", "x")
						.param("priority", "LOW")
						.param("contactName", "A")
						.param("contactEmail", "a@b.com")
						.param("contactPhone", "1"))
				.andExpect(status().isCreated())
				.andReturn();
		long id = readId(created);
		mockMvc.perform(put("/api/tickets/" + id + "/assign")
						.with(user("student1").roles("USER"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"technicianId\":2}"))
				.andExpect(status().isForbidden());
	}

	@Test
	void assignTechnician_asAdmin() throws Exception {
		MvcResult created = mockMvc.perform(multipart("/api/tickets")
						.param("category", "Other")
						.param("description", "y")
						.param("priority", "LOW")
						.param("contactName", "B")
						.param("contactEmail", "b@c.com")
						.param("contactPhone", "2"))
				.andExpect(status().isCreated())
				.andReturn();
		long id = readId(created);
		mockMvc.perform(put("/api/tickets/" + id + "/assign")
						.with(user("admin").roles("ADMIN"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"technicianId\":2}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.assignedTechnician.username").value("tech1"));
	}

	@Test
	void deleteComment_forbiddenWhenNotOwnerOrAdmin() throws Exception {
		MvcResult created = mockMvc.perform(multipart("/api/tickets")
						.param("category", "Other")
						.param("description", "z")
						.param("priority", "LOW")
						.param("contactName", "C")
						.param("contactEmail", "c@d.com")
						.param("contactPhone", "3"))
				.andExpect(status().isCreated())
				.andReturn();
		long ticketId = readId(created);
		MvcResult afterComment = mockMvc.perform(post("/api/tickets/" + ticketId + "/comments")
						.with(user("student1").roles("USER"))
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"text\":\"Owner comment\"}"))
				.andExpect(status().isOk())
				.andReturn();
		long commentId = objectMapper.readTree(afterComment.getResponse().getContentAsString())
				.path("comments")
				.get(0)
				.path("id")
				.asLong();
		mockMvc.perform(delete("/api/tickets/" + ticketId + "/comments/" + commentId)
						.with(user("tech1").roles("TECHNICIAN")))
				.andExpect(status().isForbidden());
	}

	private long readId(MvcResult result) throws Exception {
		JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
		return root.path("id").asLong();
	}
}
