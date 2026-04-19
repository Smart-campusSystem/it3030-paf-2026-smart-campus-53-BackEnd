package com.smart_campus_system.demo.security;

import org.springframework.stereotype.Service;

import com.smart_campus_system.demo.model.Role;
import com.smart_campus_system.demo.repository.TicketCommentRepository;
import com.smart_campus_system.demo.repository.UserRepository;

@Service("commentSecurity")
public class CommentSecurityService {

	private final TicketCommentRepository commentRepository;
	private final UserRepository userRepository;

	public CommentSecurityService(TicketCommentRepository commentRepository, UserRepository userRepository) {
		this.commentRepository = commentRepository;
		this.userRepository = userRepository;
	}

	public boolean canModify(Long commentId, String username) {
		var comment = commentRepository.findById(commentId).orElse(null);
		if (comment == null) {
			return false;
		}
		var user = userRepository.findByEmail(username).orElse(null);
		if (user == null) {
			return false;
		}
		if (user.getRole() == Role.ADMIN) {
			return true;
		}
		return comment.getAuthor().getId().equals(user.getId());
	}
}
