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
        // We calculate the RAW cosine similarity (1 - cosine distance)
        String sql = "SELECT user_id, headline, resume_text, substring(resume_text from 1 for 150) as text_preview, " +
                "(1 - (resume_embedding <=> ?::vector)) as raw_similarity " +
                "FROM candidate_profiles " +
                "WHERE resume_embedding IS NOT NULL " +
                "ORDER BY resume_embedding <=> ?::vector " +
                "LIMIT ?";

        return jdbcTemplate.query(sql, (ResultSet rs, int rowNum) -> {
            CandidateMatch match = new CandidateMatch();
            match.setUserId(rs.getLong("user_id"));
            match.setHeadline(rs.getString("headline"));
            match.setResumeTextPreview(rs.getString("text_preview") + "...");
            match.setFullResumeText(rs.getString("resume_text"));
            
            // Apply human-readable normalization
            double rawSim = rs.getDouble("raw_similarity");
            match.setMatchPercentage(normalizeMatchScore(rawSim));
            
            return match;
        }, jobVectorString, jobVectorString, limit);
    }

    public Double getSingleMatchPercentage(String jobVectorString, Long userId) {
        String sql = "SELECT (1 - (resume_embedding <=> ?::vector)) as raw_similarity " +
                "FROM candidate_profiles WHERE user_id = ?";
        try{
            Double rawSim = jdbcTemplate.queryForObject(sql, Double.class, jobVectorString, userId);
            return rawSim != null ? normalizeMatchScore(rawSim) : 0.0;
        }
        catch (Exception e) {
            return 0.0;
        }
    }

    /**
     * OpenAI embeddings typically range from 0.72 (terrible match) to 0.88 (perfect match).
     * This method mathematically scales that narrow range into a 0-100% human-intuitive scale.
     */
    private double normalizeMatchScore(double rawSimilarity) {
        double score;
        if (rawSimilarity <= 0.73) {
            // Map anything below 0.73 to a very low score (0-20%)
            score = Math.max(0.0, (rawSimilarity - 0.65) / (0.73 - 0.65) * 20.0);
        } else if (rawSimilarity <= 0.77) {
            // Map 0.73 - 0.77 to 20% - 60% (Poor to Below Average)
            score = 20.0 + (rawSimilarity - 0.73) / 0.04 * 40.0;
        } else if (rawSimilarity <= 0.81) {
            // Map 0.77 - 0.81 to 60% - 80% (Average to Good)
            score = 60.0 + (rawSimilarity - 0.77) / 0.04 * 20.0;
        } else if (rawSimilarity <= 0.85) {
            // Map 0.81 - 0.85 to 80% - 90% (Good to Great)
            score = 80.0 + (rawSimilarity - 0.81) / 0.04 * 10.0;
        } else {
            // Map 0.85+ to 90% - 100% (Outstanding)
            score = 90.0 + (rawSimilarity - 0.85) / 0.05 * 10.0;
        }
        
        // Clamp between 0 and 100 to be safe
        score = Math.max(0.0, Math.min(100.0, score));
        // Round to 2 decimal places
        return Math.round(score * 100.0) / 100.0;
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
