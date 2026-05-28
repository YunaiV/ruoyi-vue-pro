# RuoYi Upstream Pin

Source repository: https://github.com/YunaiV/ruoyi-vue-pro
Selected branch: master-jdk17
Selected commit: 74b73e4c777b80bab2cdffcec3079886f0a2e98f
Selection reason: full RuoYi-Vue-Pro product base with JDK 17 compatibility for Yaya's long-term member/payment/admin platform.

Current Yaya source repository: /Volumes/LamarHD/Yaya/YAYA-pages-for-Windows-version-dev
Current Yaya commit: d073a8637bf76979b10747d9dc36281108cade89

Migration strategy:
- RuoYi owns admin, member, payment, orders, permissions, and system configuration.
- Yaya-specific content/practice/scoring orchestration goes into yudao-module-yaya.
- Python remains an independent yaya-ai-service.
