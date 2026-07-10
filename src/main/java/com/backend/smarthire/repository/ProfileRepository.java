package com.backend.smarthire.repository;

import com.backend.smarthire.model.CandidateMatch;
import com.backend.smarthire.model.CandidateProfile;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.util.List;

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
                "headline = COALESCE(EXCLUDED.headline, candidate_profiles.headline), " +
                "skills = COALESCE(EXCLUDED.skills, candidate_profiles.skills), " +
                "resume_text = COALESCE(EXCLUDED.resume_text, candidate_profiles.resume_text), " +
                "updated_at = CURRENT_TIMESTAMP";

        jdbcTemplate.update(sql,
                profile.getUserId(),
                profile.getHeadline(),
                profile.getSkills(),
                profile.getResumeText()
        );
    }

    public void upsertResumeText(Long userId, String resumeText, String embeddingString){
        String sql = "INSERT INTO candidate_profiles (user_id, resume_text, resume_embedding) " +
                "VALUES (?, ?, ?::vector) " +
                "ON CONFLICT (user_id) " +
                "DO UPDATE SET " +
                "resume_text = EXCLUDED.resume_text, " +
                "resume_embedding = EXCLUDED.resume_embedding, " +
                "updated_at = CURRENT_TIMESTAMP";
        jdbcTemplate.update(sql, userId, resumeText, embeddingString);
    }

    public void upsertResumeTextOnly(Long userId, String resumeText){
        String sql = "INSERT INTO candidate_profiles (user_id, resume_text) " +
                "VALUES (?, ?) " +
                "ON CONFLICT (user_id) " +
                "DO UPDATE SET " +
                "resume_text = EXCLUDED.resume_text, " +
                "updated_at = CURRENT_TIMESTAMP";
        jdbcTemplate.update(sql, userId, resumeText);
    }

    public void updateResumeEmbedding(Long userId, String embeddingString){
        String sql = "UPDATE candidate_profiles SET " +
                "resume_embedding = ?::vector, " +
                "updated_at = CURRENT_TIMESTAMP " +
                "WHERE user_id = ?";
        jdbcTemplate.update(sql, embeddingString, userId);
    }

    public List<CandidateMatch> findSimilarCandidates(String jobVectorString, int limit) {
        // The magic SQL query using pgvector's Cosine Distance operator <=>
        String sql = "SELECT user_id, headline, resume_text, substring(resume_text from 1 for 150) as text_preview, " +
                // (1 - cosine_distance) * 100 = Match Percentage!
                "ROUND(((1 - (resume_embedding <=> ?::vector)) * 100)::numeric, 2) as match_percentage " +
                "FROM candidate_profiles " +
                // Ignore profiles that haven't uploaded a resume yet
                "WHERE resume_embedding IS NOT NULL " +
                // Order by the most mathematically similar vectors first
                "ORDER BY resume_embedding <=> ?::vector " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            CandidateMatch match = new CandidateMatch();
            match.setUserId(rs.getLong("user_id"));
            match.setHeadline(rs.getString("headline"));
            match.setResumeTextPreview(rs.getString("text_preview") + "...");
            match.setMatchPercentage(rs.getDouble("match_percentage"));
            match.setFullResumeText(rs.getString("resume_text"));
            return match;
        }, jobVectorString, jobVectorString, limit);
        // Notice we pass jobVectorString twice! Once for the SELECT percentage calculation, and once for the ORDER BY sorting.
    }

    public Double getSingleMatchPercentage(String jobVectorString, Long userId) {
        String sql = "SELECT ROUND(((1 - (resume_embedding <=> ?::vector)) * 100)::numeric, 2) " +
                "FROM candidate_profiles WHERE user_id = ?";
        try{
            return jdbcTemplate.queryForObject(sql, Double.class, jobVectorString,userId);
        }
        catch (Exception e) {
            return 0.0;
        }
    }

    public String getResumeTextByUserId(Long userId) {
        String sql = "SELECT resume_text FROM candidate_profiles WHERE user_id = ?";
        try {
            return jdbcTemplate.queryForObject(sql, String.class, userId);
        }catch (Exception e) {
            return null;
        }
    }

    public void createDeafultProfile(Long userId){
        String sql = "INSERT INTO candidate_profiles (user_id) VALUES (?) " +
                "ON CONFLICT (user_id) DO NOTHING";
        jdbcTemplate.update(sql,userId);
    }

}
