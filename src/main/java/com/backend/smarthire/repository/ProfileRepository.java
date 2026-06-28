package com.backend.smarthire.repository;

import com.backend.smarthire.model.CandidateProfile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class ProfileRepository {
    private final JdbcTemplate jdbcTemplate;

    public ProfileRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveOrUpdateProfile(CandidateProfile profile) {
        String sql = "INSERT INTO candidate_profiles (user_id, headline, skills, resume_text) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (user_id) DO UPDATE SET " +
                "headline = EXCLUDED.headline, " +
                "skills = EXCLUDED.skills, " +
                "resume_text = EXCLUDED.resume_text, " +
                "updated_at = CURRENT_TIMESTAMP";

        jdbcTemplate.update(sql,
                profile.getUserId(),
                profile.getHeadline(),
                profile.getSkills(),
                profile.getResumeText()
        );
    }

}
