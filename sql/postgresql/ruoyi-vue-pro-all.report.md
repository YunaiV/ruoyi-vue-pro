# Final merged PostgreSQL SQL report

## Included files
- ruoyi-vue-pro.sql
- quartz.sql
- ruoyi-vue-pro-modules-extra.sql

## Notes
- Repaired generated module extra SQL directly in formal project directory.
- Converted generated boolean columns to smallint with 0/1 defaults for Java/MyBatis compatibility.
- Generated merged files with and without transaction wrapper.
- Added Flowable PostgreSQL engine create SQL extracted from local Maven dependencies.
