# SmartHire

1. Turn on the vector extension in Neon:

SQL
CREATE EXTENSION IF NOT EXISTS vector;
2. Add the vector column to your table:
   OpenAI's standard embedding model outputs exactly 1,536 numbers per chunk of text. We must tell PostgreSQL exactly how big this mathematical array will be.

SQL
ALTER TABLE candidate_profiles ADD COLUMN resume_embedding vector(1536);
3. Step 3: Verify Chunk 1
Run this quick query to check your table structure:

SQL
SELECT column_name, data_type
FROM information_schema.columns
WHERE table_name = 'candidate_profiles';
