/*
 Navicat Premium Data Transfer

 Source Server         : 127.0.0.1 PostgreSQL
 Source Server Type    : PostgreSQL
 Source Server Version : 140002
 Source Host           : 127.0.0.1:5432
 Source Catalog        : ruoyi-vue-pro
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 140002
 File Encoding         : 65001

 Date: 30/04/2022 23:05:07
*/


-- ----------------------------
-- Table structure for act_evt_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_evt_log";
CREATE TABLE "public"."act_evt_log" (
  "log_nr_" int4 NOT NULL DEFAULT nextval('act_evt_log_log_nr__seq'::regclass),
  "type_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "time_stamp_" timestamp(6) NOT NULL,
  "user_id_" varchar(255) COLLATE "pg_catalog"."default",
  "data_" bytea,
  "lock_owner_" varchar(255) COLLATE "pg_catalog"."default",
  "lock_time_" timestamp(6),
  "is_processed_" int2 DEFAULT 0
)
;
ALTER TABLE "public"."act_evt_log" OWNER TO "root";

-- ----------------------------
-- Records of act_evt_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ge_bytearray
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ge_bytearray";
CREATE TABLE "public"."act_ge_bytearray" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "deployment_id_" varchar(64) COLLATE "pg_catalog"."default",
  "bytes_" bytea,
  "generated_" bool
)
;
ALTER TABLE "public"."act_ge_bytearray" OWNER TO "root";

-- ----------------------------
-- Records of act_ge_bytearray
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_ge_bytearray" ("id_", "rev_", "name_", "deployment_id_", "bytes_", "generated_") VALUES ('65288f6b-c892-11ec-a15b-3e2374911326', 2, 'source', NULL, E'<?xml version="1.0" encoding="UTF-8"?>\\012<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="diagram_Process_1645980650311" targetNamespace="http://activiti.org/bpmn"><bpmn2:process id="oa_leave" name="OA \\350\\257\\267\\345\\201\\207" isExecutable="true"><bpmn2:startEvent id="Event_1iruxim"><bpmn2:outgoing>Flow_0804gmo</bpmn2:outgoing></bpmn2:startEvent><bpmn2:userTask id="task01" name="task01"><bpmn2:incoming>Flow_0804gmo</bpmn2:incoming><bpmn2:outgoing>Flow_0cx479x</bpmn2:outgoing></bpmn2:userTask><bpmn2:sequenceFlow id="Flow_0804gmo" sourceRef="Event_1iruxim" targetRef="task01" /><bpmn2:endEvent id="Event_1mdsccz"><bpmn2:incoming>Flow_0cx479x</bpmn2:incoming></bpmn2:endEvent><bpmn2:sequenceFlow id="Flow_0cx479x" sourceRef="task01" targetRef="Event_1mdsccz" /></bpmn2:process><bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="oa_leave_di" bpmnElement="oa_leave"><bpmndi:BPMNEdge id="Flow_0cx479x_di" bpmnElement="Flow_0cx479x"><di:waypoint x="440" y="350" /><di:waypoint x="492" y="350" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id="Flow_0804gmo_di" bpmnElement="Flow_0804gmo"><di:waypoint x="288" y="350" /><di:waypoint x="340" y="350" /></bpmndi:BPMNEdge><bpmndi:BPMNShape id="Event_1iruxim_di" bpmnElement="Event_1iruxim"><dc:Bounds x="252" y="332" width="36" height="36" /></bpmndi:BPMNShape><bpmndi:BPMNShape id="task01_di" bpmnElement="task01"><dc:Bounds x="340" y="310" width="100" height="80" /></bpmndi:BPMNShape><bpmndi:BPMNShape id="Event_1mdsccz_di" bpmnElement="Event_1mdsccz"><dc:Bounds x="492" y="332" width="36" height="36" /></bpmndi:BPMNShape></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></bpmn2:definitions>', NULL);
INSERT INTO "public"."act_ge_bytearray" ("id_", "rev_", "name_", "deployment_id_", "bytes_", "generated_") VALUES ('93335d51-c892-11ec-a15b-3e2374911326', 1, 'oa_leave.bpmn', '93335d50-c892-11ec-a15b-3e2374911326', E'<?xml version="1.0" encoding="UTF-8"?>\\012<bpmn2:definitions xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:bpmn2="http://www.omg.org/spec/BPMN/20100524/MODEL" xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI" xmlns:dc="http://www.omg.org/spec/DD/20100524/DC" xmlns:di="http://www.omg.org/spec/DD/20100524/DI" id="diagram_Process_1645980650311" targetNamespace="http://activiti.org/bpmn"><bpmn2:process id="oa_leave" name="OA \\350\\257\\267\\345\\201\\207" isExecutable="true"><bpmn2:startEvent id="Event_1iruxim"><bpmn2:outgoing>Flow_0804gmo</bpmn2:outgoing></bpmn2:startEvent><bpmn2:userTask id="task01" name="task01"><bpmn2:incoming>Flow_0804gmo</bpmn2:incoming><bpmn2:outgoing>Flow_0cx479x</bpmn2:outgoing></bpmn2:userTask><bpmn2:sequenceFlow id="Flow_0804gmo" sourceRef="Event_1iruxim" targetRef="task01" /><bpmn2:endEvent id="Event_1mdsccz"><bpmn2:incoming>Flow_0cx479x</bpmn2:incoming></bpmn2:endEvent><bpmn2:sequenceFlow id="Flow_0cx479x" sourceRef="task01" targetRef="Event_1mdsccz" /></bpmn2:process><bpmndi:BPMNDiagram id="BPMNDiagram_1"><bpmndi:BPMNPlane id="oa_leave_di" bpmnElement="oa_leave"><bpmndi:BPMNEdge id="Flow_0cx479x_di" bpmnElement="Flow_0cx479x"><di:waypoint x="440" y="350" /><di:waypoint x="492" y="350" /></bpmndi:BPMNEdge><bpmndi:BPMNEdge id="Flow_0804gmo_di" bpmnElement="Flow_0804gmo"><di:waypoint x="288" y="350" /><di:waypoint x="340" y="350" /></bpmndi:BPMNEdge><bpmndi:BPMNShape id="Event_1iruxim_di" bpmnElement="Event_1iruxim"><dc:Bounds x="252" y="332" width="36" height="36" /></bpmndi:BPMNShape><bpmndi:BPMNShape id="task01_di" bpmnElement="task01"><dc:Bounds x="340" y="310" width="100" height="80" /></bpmndi:BPMNShape><bpmndi:BPMNShape id="Event_1mdsccz_di" bpmnElement="Event_1mdsccz"><dc:Bounds x="492" y="332" width="36" height="36" /></bpmndi:BPMNShape></bpmndi:BPMNPlane></bpmndi:BPMNDiagram></bpmn2:definitions>', 'f');
INSERT INTO "public"."act_ge_bytearray" ("id_", "rev_", "name_", "deployment_id_", "bytes_", "generated_") VALUES ('9342edb2-c892-11ec-a15b-3e2374911326', 1, 'oa_leave.oa_leave.png', '93335d50-c892-11ec-a15b-3e2374911326', E'\\211PNG\\015\\012\\032\\012\\000\\000\\000\\015IHDR\\000\\000\\002\\032\\000\\000\\001\\220\\010\\006\\000\\000\\000\\350z\\311\\005\\000\\000\\020\\015IDATx\\332\\355\\335[\\214\\025u\\236\\007\\360\\321\\321\\215\\032\\357;\\343\\213\\227\\030_\\234\\370d\\342\\203qM\\314\\232\\214Q\\327\\310\\214\\211\\32064\\244I\\243@\\304\\013\\261\\241]E\\0054YM|Pq\\004\\243\\211\\002b\\274\\301C\\303\\232e\\221\\036\\266\\243\\002\\212\\021\\031q\\203\\010\\312\\245\\351\\245\\033\\004\\232\\221\\006\\031\\371o\\375N\\372t\\216@#}9\\227n?\\237\\344\\237>\\247\\252N\\325\\311\\351\\252\\177}\\353_\\377\\252\\372\\315o\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\372$\\245\\364O\\2337o~g\\345\\312\\225?._\\276<-[\\266L)q\\311~\\367#\\315\\315\\315;\\232\\232\\232\\376d\\215\\004`H\\311B\\306\\273\\331N.\\355\\334\\2713uvv\\246C\\207\\016)%.\\361\\273\\307\\357\\277b\\305\\212}Y\\360\\270\\315Z\\011\\300\\220\\021-\\031\\261\\223\\263\\303/\\177imm\\335\\225\\005\\215O\\254\\225\\000\\014\\031q\\272DKF\\345\\264ldA\\343\\240\\265\\022\\200!#\\372\\010\\330\\311WN\\211\\377\\207\\265\\022\\200_]\\320\\370\\373\\336\\326\\264\\371\\323\\327\\322\\372\\017f\\346J\\274\\216a\\302\\201\\240\\001\\000\\375\\012\\032\\373\\367\\264\\244/\\227>\\226\\276\\370\\317)?+1,\\306\\011\\010\\202\\006\\000\\3649hl[\\337xL\\310\\310\\227\\355\\353\\027\\013\\010\\202\\006\\000\\364=h\\374\\357_\\237\\3511h\\3048\\001A\\320\\000\\200>\\007\\215/\\227M\\3571h\\3048\\001A\\320\\000\\000AC\\320\\000\\200\\312\\013\\032q\\225IOA#\\306\\011\\010\\202\\006\\000\\3649hl\\374\\350\\305\\036\\203F\\214\\023\\020\\004\\015\\000\\350s\\320\\330\\335\\262.}\\371\\337O\\034{\\332$\\033\\026\\343\\004\\004A\\003\\000\\372\\0344\\242|\\273f\\3361A#\\206\\011\\007\\202\\006\\000\\364/h\\034<\\2306~<\\373\\330\\323&\\331\\260\\030'' \\010\\032\\000\\320\\247\\240\\021w\\376\\374\\372\\243Y=\\366\\321\\210q\\356\\016*h\\000@\\357\\202\\306\\301\\203\\251ucS\\372\\333\\177=\\322c\\310\\310\\227\\230&\\246\\325\\272!h\\000\\300/\\006\\215_j\\305\\320\\272!h\\000@\\237\\203\\306\\311\\264b\\234\\250uC`\\0204\\000\\240\\307\\240\\321\\327\\220\\221/\\002\\203\\240\\001\\000=\\006\\015E\\320(\\227\\332\\332\\332\\363\\357\\272\\353\\256;\\253\\252\\252^\\310\\376.\\317\\312\\267Y9\\220\\225\\324\\3657\\336/\\357\\032\\177gLo+\\006Jj\\337\\276}\\027666\\316|\\361\\305\\027\\277\\2301c\\306\\256\\206\\206\\206\\316\\211\\023''\\036\\211\\212j\\302\\204\\011\\207\\353\\353\\353;\\036}\\364\\321\\2553g\\316\\\\<e\\312\\224[\\262\\217\\234"h\\330\\301\\013\\032\\345U]]}S\\266\\215.\\312\\312\\241\\256Pq\\262%\\246_\\024\\237W\\373\\001E\\265z\\365\\352\\011\\317=\\367\\\\\\313\\370\\361\\343S\\026.\\322\\273\\357\\276\\233>\\373\\354\\263\\264i\\323\\246\\264{\\367\\356\\024\\342o\\274\\217\\3411~\\352\\324\\251G\\352\\352\\352\\016e\\201c\\351\\310\\221#\\257\\0204\\024A\\243\\344\\001\\343\\272,(\\254\\352e\\270\\350\\251\\254\\212\\371\\251\\015\\201\\001\\265n\\335\\272\\177{\\351\\245\\227v\\334s\\317=i\\341\\302\\205\\251\\275\\275=\\365FL\\037\\237\\313\\002\\307\\217\\223''O~''\\253\\250~''h(\\202Fq\\325\\326\\326\\236QUU\\365\\227,\\034\\0349:0<\\370\\340\\203i\\301\\202\\005''<P\\210\\3611\\335q\\302\\306\\221\\230o\\314_\\355\\010\\364KV\\357\\374\\366\\375\\367\\337o\\214\\2001w\\356\\334\\264\\177\\377\\376\\324\\037\\361\\371\\230OVA\\035\\250\\251\\251\\371\\263\\240\\241\\010\\032\\305\\221\\205\\201K\\263@\\377Ea@\\0309rdz\\371\\345\\227\\323\\366\\355\\333{\\265\\335\\306\\364\\361\\271\\370\\374Q\\201cm,GM\\011\\3645d\\234?\\177\\376\\374\\257\\357\\275\\367\\336\\264y\\363\\3464\\220b~Yx\\351\\254\\253\\253\\233!h(\\202\\306\\300\\312\\002\\306\\037\\262\\322R\\030\\012\\236y\\346\\231\\264m\\333\\266~m\\267\\361\\371\\230\\317Qac[,O\\215\\011\\364:d\\274\\360\\302\\013{\\246M\\233\\226\\366\\354\\331\\223\\212!\\346\\333\\320\\320p`\\314\\2301\\363\\004\\015E\\320\\030\\320\\226\\214\\226\\302V\\214\\306\\306\\306\\001\\335vc~G\\265nl\\323\\262\\001\\364&d\\374v\\376\\374\\371\\033#dD\\245\\\\L1\\377\\372\\372\\372\\275\\243F\\215zT\\320P\\004\\215\\376\\211>\\023\\205\\247K\\306\\216\\035\\233>\\377\\374\\363\\242l\\2731\\337\\230\\177\\341i\\024}6\\200\\223\\022}2&M\\232T\\264\\226\\214\\343\\265l\\214\\0337\\256#\\253\\250\\206\\011\\032\\212\\240\\321w]\\035?\\273[2\\212\\0252\\012\\303Fa\\313F,_\\015\\012\\234PVq\\334\\026\\035?\\007\\272O\\306\\311\\364\\331\\250\\251\\251\\3313|\\370\\360\\337\\013\\032\\212\\240\\321{]\\227\\260\\376\\224\\337\\351/^\\274\\270$\\333n,\\247\\240U\\343''\\227\\276\\002''\\024\\227\\260\\306U!\\3450{\\366\\354\\035YE\\365\\274\\240\\241\\010\\032\\275Wx\\237\\214\\350\\260YJGu\\020]\\245&\\005\\216+n\\306\\025\\255\\031\\035\\035\\035e\\011\\032\\261\\334\\321\\243G\\357\\033\\2127\\365\\0224\\004\\215b\\0321b\\304\\037\\013O\\231\\364\\367\\352\\222\\276\\\\\\215Rx\\012%\\276\\217\\032\\0258F\\334\\361s\\321\\242E\\251\\234\\346\\315\\233\\267)\\253\\250^\\0264\\024A\\243W\\255\\031\\213\\362;\\371\\270\\337E9\\304r\\013Z5\\026\\251Q\\201\\237\\351\\350\\350\\370\\347\\011\\023&\\244\\266\\266\\266\\262\\006\\215l\\371\\377\\310\\216\\214vN\\237>\\375TAC\\0214~Y\\266\\275\\\\P\\370\\354\\222R\\267f\\024\\266j\\024>\\033%\\276\\227\\232\\025\\350\\266d\\311\\222\\031\\361\\354\\222J0n\\334\\270\\357\\262\\212\\352_\\006\\311\\221\\344_\\263\\362\\257\\202\\206\\240Q\\256\\365+\\236\\256Zx[\\361r:\\352v\\345w\\252Y\\201n\\263f\\315\\372[<\\000\\255\\022\\314\\2313''nk\\374\\364 \\331\\021\\344+\\325\\023\\356\\020\\004\\015A\\243X\\353W6|V~\\2727\\336x\\243\\254\\333n,\\277\\340;\\317R\\263\\002\\335\\342Q\\357\\361\\020\\245J\\320\\334\\334\\274\\256\\252\\252\\252q\\220\\355\\010N\\270C\\0204\\004\\215b\\255_\\325\\325\\325M\\371\\361k\\326\\254)\\353\\266\\033\\313\\317\\177\\227\\370^jV\\240\\333\\324\\251S;K}\\357\\214\\236l\\330\\260!N\\235|:Hw\\004\\307\\335!\\010\\032\\202F\\261\\326\\257\\354\\357\\326\\374\\360ro\\303\\261\\374\\202\\357\\270U\\315\\012t\\2330a\\302\\221\\357\\277\\377\\276"\\202\\306\\356\\335\\273\\367g\\225T\\313 \\337\\021\\374l\\207 h\\010\\032E\\\\\\277~\\310\\277/\\3676\\034\\313/h\\321\\350P\\263\\002\\335F\\217\\036\\235\\016\\037>\\\\\\021A#\\373\\036\\035''Q\\301\\016\\252"hTV\\031j\\353W\\276\\224{\\033\\216\\345\\027|\\237\\177\\250Y\\201n\\343\\307\\217?\\\\)-\\032\\355\\355\\355\\337\\014\\201\\026\\215_\\355\\251\\223\\203\\007\\017\\036w\\370\\276}\\373Rgg\\247\\026\\215\\001^\\277\\242\\345@\\213\\006P\\361\\036z\\350\\241\\216J\\351\\243\\361\\325W_\\255\\031\\304}4*\\2723h\\204\\200W_}5-X\\260\\240O\\237\\177\\353\\255\\267b\\007\\235\\026.\\\\x\\314\\270\\247\\236z*\\235w\\336y\\351\\334s\\317M\\217=\\366\\330\\317\\306\\275\\366\\332k\\271\\317\\315\\236=[\\320\\030\\340\\365K\\037\\015`P\\2306m\\332\\266J\\271\\352\\344\\203\\017>X>\\010\\257:\\031\\024\\227\\267~\\374\\361\\307\\271\\035\\376\\263\\317>\\333\\257\\240\\361\\336{\\357\\375l\\370\\332\\265k\\323)\\247\\234\\222n\\275\\365\\326t\\373\\355\\267\\347\\246Y\\265jU\\332\\277\\177\\177\\3569\\030g\\235u\\226\\240Q\\244\\365\\313U''\\300\\2400}\\372\\364\\305\\225r\\037\\215\\354\\310x\\321 \\272\\217\\306\\240\\272a\\327\\325W_\\235\\333\\341_x\\341\\205i\\326\\254Y\\271S\\031\\343\\306\\215K\\027_|q:\\343\\2143\\322\\2157\\336\\230ZZZRkkk\\0325jT\\272\\350\\242\\213\\322\\331g\\237\\235jkks\\255!\\205A\\343\\225W^I\\227_~yz\\340\\201\\007\\322\\323O?\\235\\033\\036A&\\036\\035\\036\\257\\237x\\342\\211\\264z\\365\\352\\334|\\207\\015\\033&h\\024i\\375r\\037\\015`Phhh\\270y\\352\\324\\251G*\\241/\\350\\310\\221#\\327\\015\\226;\\203\\236\\254J\\011\\032S\\246L\\311\\355\\360\\243\\345\\241\\271\\2719566\\346Nu<\\376\\370\\343\\351\\276\\373\\356\\313\\215\\213\\0001m\\332\\264\\334\\3538\\315\\022\\257\\257\\272\\352\\252\\334\\364\\371\\2401q\\342\\304t\\372\\351\\247\\247\\033n\\270!\\327\\367\\342\\376\\373\\357\\317\\015\\337\\270qc\\332\\276}{\\356u<\\240o\\307\\216\\035\\351\\353\\257\\277\\216V*A\\243xa\\244b\\356\\014\\232\\205\\316#\\356\\014\\012\\364\\324\\242qjvd{\\250\\334\\317:\\311vR\\037e\\025T\\253g\\235\\224\\356\\324ISSS\\312Bf\\272\\346\\232kr\\343f\\316\\234\\231\\226,Y\\222{\\035\\247<n\\272\\351\\246\\\\@\\370\\341\\207\\037\\272\\203F\\276|\\370\\341\\207\\271yL\\2324)\\367\\376\\233o\\276\\351\\016\\032\\321R\\222_\\206\\240Q<\\236u\\002\\014\\032\\331\\321\\356\\322r?\\275\\365\\311''\\237|\\323\\323[K\\0274\\242\\365"\\337\\302\\021\\255\\032\\361:\\373\\037tw\\340\\274\\343\\216;r-\\0361<N\\265\\344\\203\\306\\025W\\\\\\221\\316<\\363\\314t\\363\\3157\\347\\246\\215\\317\\304\\3608U\\262n\\335\\272\\334\\353\\302\\016\\241\\202F\\321[5<\\275\\025\\030\\024GFW\\324\\325\\325\\375\\330\\321\\321Q\\226\\212j\\317\\236=\\237d\\025T[|\\017A\\2438%:\\353\\305\\016?\\373\\215St\\376\\215\\323\\033\\361~\\351\\322\\245\\351\\221G\\036\\311\\275\\216S%\\361\\200\\275\\013.\\270 -^\\2748\\327\\037#\\206G\\263|>h\\274\\375\\366\\333\\351\\341\\207\\037\\316\\275\\216\\323/qZ%^WUU\\2451c\\306\\344^GK\\211\\240Q\\032#F\\214\\370c~''\\037\\377\\333R\\267j\\304\\362\\252\\253\\253\\273O\\233\\304\\367Q\\243\\002\\3075y\\362\\344w\\346\\316\\235[\\226\\240\\221-\\373\\355\\254\\222z~(\\376\\256\\225\\0224\\2620\\227.\\271\\344\\222\\334N\\277\\276\\276>\\255\\\\\\2712]v\\331e\\271\\367\\327^{m:\\347\\234sr\\255\\030[\\266lI\\327_\\177}:\\355\\264\\323r-\\027\\267\\334rK\\356\\224H\\341\\345\\255\\273v\\355\\312u\\026\\275\\362\\312+s\\235J\\343\\364\\311\\251\\247\\236\\232\\273\\372\\244\\360\\264\\211\\240Q\\262V\\215U\\371\\035}\\\\\\351SJ\\261\\274\\202\\326\\214UjR\\240G\\303\\207\\017\\377}mm\\355\\201R_\\217\\237\\035i\\307)\\223\\266X\\276\\240Q\\334\\022}-"4\\024\\336[#\\25649\\336\\264\\273w\\357N{\\367\\356=\\351y\\307\\364\\355\\355\\355nA^\\006\\325\\325\\325\\327e\\333\\320O\\371\\035~\\264F\\225B,\\247 d\\374\\024\\337CM\\012\\234PMM\\315\\237\\307\\217\\037\\337\\031G\\277\\245\\320\\326\\326\\366?Y\\005\\265%+\\303\\206\\352o\\352\\026\\344\\202F)TUU\\375\\245\\360\\024J\\\\j\\\\L1\\377\\302S&\\261|5(pR\\352\\352\\352f444\\034\\210J\\271\\230\\262\\243\\351\\257\\262\\012\\361\\323\\254\\262\\372\\367\\241\\374{\\012\\032\\202F)\\324\\326\\326\\236\\221\\355\\360\\327\\346w\\374c\\307\\216-Z\\330\\210\\371f\\313\\373\\251\\2405cm,_\\355\\011\\234\\2641c\\306\\314\\253\\257\\257\\337[\\254\\226\\215\\266\\266\\266\\025\\0212\\262\\012\\352\\325\\241\\376[\\012\\032\\202F\\251d\\333\\323\\245Y\\331V\\330\\262\\021\\035v\\007R\\314\\257\\260%\\243ky\\227\\2525\\201^\\0335j\\324\\243uuu\\003\\376\\034\\224\\256>\\031[\\207zK\\206\\240!h\\224C\\266]\\375\\2410l\\344;\\210\\366\\367j\\224\\370\\374Q\\035?s!#\\226\\247\\266\\004\\372s\\2044\\254\\246\\246f\\317\\354\\331\\263w\\304\\263+\\372c\\357\\336\\275\\253\\273\\256.i\\033\\312}2\\004\\015A\\243BZ6\\326\\026\\206\\202h\\335\\210\\373]Dg\\340^\\336H/\\367\\271\\243Z1R\\327\\374\\265d\\000\\003r\\204\\364\\273\\270\\364t\\364\\350\\321\\373^\\177\\375\\365M\\355\\355\\355\\207{s[\\361\\226\\226\\226\\217f\\314\\230\\361fW\\300x>\\346\\367k\\372\\375\\004\\015A\\243\\034\\242\\317DW\\007\\321\\243\\003B\\356\\276(\\361\\024\\337\\270\\237\\312\\246M\\233rW\\014\\205\\370\\033\\357cx\\214?\\352\\266\\342\\371r$\\346\\253O\\0060\\340\\342fZq\\347\\316,(\\264\\335}\\367\\335[\\346\\314\\231\\363yss\\363\\272\\015\\0336l\\335\\265k\\327\\337\\263zj\\177[[\\333\\346\\365\\353\\327\\257\\311*\\363e\\361\\200\\264\\256g\\227\\374_|n(\\336\\214K\\320\\0204\\006\\301\\201\\302u\\205\\367\\331\\350gY\\345\\022V\\240\\024N\\0311b\\304\\365Y\\205\\363\\037\\361H\\367\\254\\362\\371\\244\\340\\234p\\374\\375$\\206\\307\\370\\230.\\246\\3775\\377X\\202\\206\\240Q!\\201\\343\\246\\256\\333\\225\\037\\352e\\270\\210\\351\\027\\305\\347U}\\000\\202\\206"h\\234Pmm\\355\\371\\361t\\325\\354`\\340\\205\\354\\357\\362\\254|\\233\\225\\003]\\241\\342@\\327\\373\\345]\\343\\357\\214\\351m\\305\\000\\202\\206"h\\000 h(\\202\\006\\000\\010\\032\\202\\006\\000\\010\\032\\212\\240\\001\\200\\240a\\007/h\\000\\200\\240!h\\000\\200\\240\\241\\010\\032\\000 h\\010\\032\\000 h\\010\\032\\000 h(\\202\\006\\000\\010\\032\\202\\006\\000\\010\\032\\202\\006\\000\\010\\032\\212\\240\\001\\000\\202\\206\\240\\001\\000\\202\\206\\240\\001\\000\\202\\206"h\\000\\200\\240!h\\000@\\271,_\\276\\374Hgg\\247\\235|\\005\\224\\354\\377\\260#\\013\\032\\007\\255\\225\\000\\014\\031\\315\\315\\315;v\\356\\334iG_\\001e\\313\\226-oeA\\343\\023k%\\000CFSS\\323\\237V\\254X\\261\\257\\265\\265u\\227\\226\\215\\262\\265d\\264~\\367\\335wof!ckVn\\263V\\0020\\244\\304\\316-\\216\\244\\263r(\\372\\010(%/\\207\\272~\\177!\\003\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\000\\250|\\377\\017\\257\\316\\344>\\022>xz\\000\\000\\000\\000IEND\\256B`\\202', 't');
COMMIT;

-- ----------------------------
-- Table structure for act_ge_property
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ge_property";
CREATE TABLE "public"."act_ge_property" (
  "name_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "value_" varchar(300) COLLATE "pg_catalog"."default",
  "rev_" int4
)
;
ALTER TABLE "public"."act_ge_property" OWNER TO "root";

-- ----------------------------
-- Records of act_ge_property
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('common.schema.version', '6.7.0.0', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('next.dbid', '1', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('identitylink.schema.version', '6.7.0.0', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('entitylink.schema.version', '6.7.0.0', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('eventsubscription.schema.version', '6.7.0.0', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('task.schema.version', '6.7.0.0', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('variable.schema.version', '6.7.0.0', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('job.schema.version', '6.7.0.0', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('batch.schema.version', '6.7.0.0', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('schema.version', '6.7.0.0', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('schema.history', 'create(6.7.0.0)', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('cfg.execution-related-entities-count', 'true', 1);
INSERT INTO "public"."act_ge_property" ("name_", "value_", "rev_") VALUES ('cfg.task-related-entities-count', 'true', 1);
COMMIT;

-- ----------------------------
-- Table structure for act_hi_actinst
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_actinst";
CREATE TABLE "public"."act_hi_actinst" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4 DEFAULT 1,
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "act_id_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "call_proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "act_name_" varchar(255) COLLATE "pg_catalog"."default",
  "act_type_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "assignee_" varchar(255) COLLATE "pg_catalog"."default",
  "start_time_" timestamp(6) NOT NULL,
  "end_time_" timestamp(6),
  "transaction_order_" int4,
  "duration_" int8,
  "delete_reason_" varchar(4000) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_hi_actinst" OWNER TO "root";

-- ----------------------------
-- Records of act_hi_actinst
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_hi_actinst" ("id_", "rev_", "proc_def_id_", "proc_inst_id_", "execution_id_", "act_id_", "task_id_", "call_proc_inst_id_", "act_name_", "act_type_", "assignee_", "start_time_", "end_time_", "transaction_order_", "duration_", "delete_reason_", "tenant_id_") VALUES ('0d28119b-c893-11ec-8a1d-3e2374911326', 1, 'oa_leave:1:934362e3-c892-11ec-a15b-3e2374911326', '0d23cbd6-c893-11ec-8a1d-3e2374911326', '0d28119a-c893-11ec-8a1d-3e2374911326', 'Event_1iruxim', NULL, NULL, NULL, 'startEvent', NULL, '2022-04-30 22:37:24.812', '2022-04-30 22:37:24.818', 1, 6, NULL, '');
INSERT INTO "public"."act_hi_actinst" ("id_", "rev_", "proc_def_id_", "proc_inst_id_", "execution_id_", "act_id_", "task_id_", "call_proc_inst_id_", "act_name_", "act_type_", "assignee_", "start_time_", "end_time_", "transaction_order_", "duration_", "delete_reason_", "tenant_id_") VALUES ('0d29712c-c893-11ec-8a1d-3e2374911326', 1, 'oa_leave:1:934362e3-c892-11ec-a15b-3e2374911326', '0d23cbd6-c893-11ec-8a1d-3e2374911326', '0d28119a-c893-11ec-8a1d-3e2374911326', 'Flow_0804gmo', NULL, NULL, NULL, 'sequenceFlow', NULL, '2022-04-30 22:37:24.821', '2022-04-30 22:37:24.821', 2, 0, NULL, '');
INSERT INTO "public"."act_hi_actinst" ("id_", "rev_", "proc_def_id_", "proc_inst_id_", "execution_id_", "act_id_", "task_id_", "call_proc_inst_id_", "act_name_", "act_type_", "assignee_", "start_time_", "end_time_", "transaction_order_", "duration_", "delete_reason_", "tenant_id_") VALUES ('7bc1aa95-c893-11ec-8080-3e2374911326', 1, 'oa_leave:1:934362e3-c892-11ec-a15b-3e2374911326', '0d23cbd6-c893-11ec-8a1d-3e2374911326', '0d28119a-c893-11ec-8a1d-3e2374911326', 'Flow_0cx479x', NULL, NULL, NULL, 'sequenceFlow', NULL, '2022-04-30 22:40:30.368', '2022-04-30 22:40:30.368', 1, 0, NULL, '');
INSERT INTO "public"."act_hi_actinst" ("id_", "rev_", "proc_def_id_", "proc_inst_id_", "execution_id_", "act_id_", "task_id_", "call_proc_inst_id_", "act_name_", "act_type_", "assignee_", "start_time_", "end_time_", "transaction_order_", "duration_", "delete_reason_", "tenant_id_") VALUES ('7bc1f8b6-c893-11ec-8080-3e2374911326', 1, 'oa_leave:1:934362e3-c892-11ec-a15b-3e2374911326', '0d23cbd6-c893-11ec-8a1d-3e2374911326', '0d28119a-c893-11ec-8a1d-3e2374911326', 'Event_1mdsccz', NULL, NULL, NULL, 'endEvent', NULL, '2022-04-30 22:40:30.37', '2022-04-30 22:40:30.372', 2, 2, NULL, '');
INSERT INTO "public"."act_hi_actinst" ("id_", "rev_", "proc_def_id_", "proc_inst_id_", "execution_id_", "act_id_", "task_id_", "call_proc_inst_id_", "act_name_", "act_type_", "assignee_", "start_time_", "end_time_", "transaction_order_", "duration_", "delete_reason_", "tenant_id_") VALUES ('0d29983d-c893-11ec-8a1d-3e2374911326', 2, 'oa_leave:1:934362e3-c892-11ec-a15b-3e2374911326', '0d23cbd6-c893-11ec-8a1d-3e2374911326', '0d28119a-c893-11ec-8a1d-3e2374911326', 'task01', '0d2f167e-c893-11ec-8a1d-3e2374911326', NULL, 'task01', 'userTask', '1', '2022-04-30 22:37:24.822', '2022-04-30 22:40:30.359', 3, 185537, NULL, '');
COMMIT;

-- ----------------------------
-- Table structure for act_hi_attachment
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_attachment";
CREATE TABLE "public"."act_hi_attachment" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "user_id_" varchar(255) COLLATE "pg_catalog"."default",
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "description_" varchar(4000) COLLATE "pg_catalog"."default",
  "type_" varchar(255) COLLATE "pg_catalog"."default",
  "task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "url_" varchar(4000) COLLATE "pg_catalog"."default",
  "content_id_" varchar(64) COLLATE "pg_catalog"."default",
  "time_" timestamp(6)
)
;
ALTER TABLE "public"."act_hi_attachment" OWNER TO "root";

-- ----------------------------
-- Records of act_hi_attachment
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_hi_comment
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_comment";
CREATE TABLE "public"."act_hi_comment" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "type_" varchar(255) COLLATE "pg_catalog"."default",
  "time_" timestamp(6) NOT NULL,
  "user_id_" varchar(255) COLLATE "pg_catalog"."default",
  "task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "action_" varchar(255) COLLATE "pg_catalog"."default",
  "message_" varchar(4000) COLLATE "pg_catalog"."default",
  "full_msg_" bytea
)
;
ALTER TABLE "public"."act_hi_comment" OWNER TO "root";

-- ----------------------------
-- Records of act_hi_comment
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_hi_detail
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_detail";
CREATE TABLE "public"."act_hi_detail" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "type_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "act_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "name_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "var_type_" varchar(64) COLLATE "pg_catalog"."default",
  "rev_" int4,
  "time_" timestamp(6) NOT NULL,
  "bytearray_id_" varchar(64) COLLATE "pg_catalog"."default",
  "double_" float8,
  "long_" int8,
  "text_" varchar(4000) COLLATE "pg_catalog"."default",
  "text2_" varchar(4000) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_hi_detail" OWNER TO "root";

-- ----------------------------
-- Records of act_hi_detail
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_hi_detail" ("id_", "type_", "proc_inst_id_", "execution_id_", "task_id_", "act_inst_id_", "name_", "var_type_", "rev_", "time_", "bytearray_id_", "double_", "long_", "text_", "text2_") VALUES ('0d27ea89-c893-11ec-8a1d-3e2374911326', 'VariableUpdate', '0d23cbd6-c893-11ec-8a1d-3e2374911326', '0d23cbd6-c893-11ec-8a1d-3e2374911326', NULL, NULL, 'day', 'long', 0, '2022-04-30 22:37:24.811', NULL, NULL, 15, '15', NULL);
COMMIT;

-- ----------------------------
-- Table structure for act_hi_entitylink
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_entitylink";
CREATE TABLE "public"."act_hi_entitylink" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "link_type_" varchar(255) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6),
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "parent_element_id_" varchar(255) COLLATE "pg_catalog"."default",
  "ref_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "ref_scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "ref_scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "root_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "root_scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "hierarchy_type_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_hi_entitylink" OWNER TO "root";

-- ----------------------------
-- Records of act_hi_entitylink
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_hi_identitylink
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_identitylink";
CREATE TABLE "public"."act_hi_identitylink" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id_" varchar(255) COLLATE "pg_catalog"."default",
  "type_" varchar(255) COLLATE "pg_catalog"."default",
  "user_id_" varchar(255) COLLATE "pg_catalog"."default",
  "task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6),
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_hi_identitylink" OWNER TO "root";

-- ----------------------------
-- Records of act_hi_identitylink
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_hi_identitylink" ("id_", "group_id_", "type_", "user_id_", "task_id_", "create_time_", "proc_inst_id_", "scope_id_", "sub_scope_id_", "scope_type_", "scope_definition_id_") VALUES ('0d23f2e7-c893-11ec-8a1d-3e2374911326', NULL, 'starter', '1', NULL, '2022-04-30 22:37:24.786', '0d23cbd6-c893-11ec-8a1d-3e2374911326', NULL, NULL, NULL, NULL);
INSERT INTO "public"."act_hi_identitylink" ("id_", "group_id_", "type_", "user_id_", "task_id_", "create_time_", "proc_inst_id_", "scope_id_", "sub_scope_id_", "scope_type_", "scope_definition_id_") VALUES ('0d399dcf-c893-11ec-8a1d-3e2374911326', NULL, 'assignee', '1', '0d2f167e-c893-11ec-8a1d-3e2374911326', '2022-04-30 22:37:24.927', NULL, NULL, NULL, NULL, NULL);
INSERT INTO "public"."act_hi_identitylink" ("id_", "group_id_", "type_", "user_id_", "task_id_", "create_time_", "proc_inst_id_", "scope_id_", "sub_scope_id_", "scope_type_", "scope_definition_id_") VALUES ('0d3c5cf0-c893-11ec-8a1d-3e2374911326', NULL, 'participant', '1', NULL, '2022-04-30 22:37:24.945', '0d23cbd6-c893-11ec-8a1d-3e2374911326', NULL, NULL, NULL, NULL);
INSERT INTO "public"."act_hi_identitylink" ("id_", "group_id_", "type_", "user_id_", "task_id_", "create_time_", "proc_inst_id_", "scope_id_", "sub_scope_id_", "scope_type_", "scope_definition_id_") VALUES ('7bba5794-c893-11ec-8080-3e2374911326', NULL, 'participant', '1', NULL, '2022-04-30 22:40:30.321', '0d23cbd6-c893-11ec-8a1d-3e2374911326', NULL, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for act_hi_procinst
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_procinst";
CREATE TABLE "public"."act_hi_procinst" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4 DEFAULT 1,
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "business_key_" varchar(255) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "start_time_" timestamp(6) NOT NULL,
  "end_time_" timestamp(6),
  "duration_" int8,
  "start_user_id_" varchar(255) COLLATE "pg_catalog"."default",
  "start_act_id_" varchar(255) COLLATE "pg_catalog"."default",
  "end_act_id_" varchar(255) COLLATE "pg_catalog"."default",
  "super_process_instance_id_" varchar(64) COLLATE "pg_catalog"."default",
  "delete_reason_" varchar(4000) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "callback_id_" varchar(255) COLLATE "pg_catalog"."default",
  "callback_type_" varchar(255) COLLATE "pg_catalog"."default",
  "reference_id_" varchar(255) COLLATE "pg_catalog"."default",
  "reference_type_" varchar(255) COLLATE "pg_catalog"."default",
  "propagated_stage_inst_id_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_hi_procinst" OWNER TO "root";

-- ----------------------------
-- Records of act_hi_procinst
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_hi_procinst" ("id_", "rev_", "proc_inst_id_", "business_key_", "proc_def_id_", "start_time_", "end_time_", "duration_", "start_user_id_", "start_act_id_", "end_act_id_", "super_process_instance_id_", "delete_reason_", "tenant_id_", "name_", "callback_id_", "callback_type_", "reference_id_", "reference_type_", "propagated_stage_inst_id_") VALUES ('0d23cbd6-c893-11ec-8a1d-3e2374911326', 3, '0d23cbd6-c893-11ec-8a1d-3e2374911326', '1', 'oa_leave:1:934362e3-c892-11ec-a15b-3e2374911326', '2022-04-30 22:37:24.784', '2022-04-30 22:40:30.545', 185761, '1', 'Event_1iruxim', 'Event_1mdsccz', NULL, NULL, '', 'OA 请假', NULL, NULL, NULL, NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for act_hi_taskinst
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_taskinst";
CREATE TABLE "public"."act_hi_taskinst" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4 DEFAULT 1,
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "task_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "task_def_key_" varchar(255) COLLATE "pg_catalog"."default",
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "propagated_stage_inst_id_" varchar(255) COLLATE "pg_catalog"."default",
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "parent_task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "description_" varchar(4000) COLLATE "pg_catalog"."default",
  "owner_" varchar(255) COLLATE "pg_catalog"."default",
  "assignee_" varchar(255) COLLATE "pg_catalog"."default",
  "start_time_" timestamp(6) NOT NULL,
  "claim_time_" timestamp(6),
  "end_time_" timestamp(6),
  "duration_" int8,
  "delete_reason_" varchar(4000) COLLATE "pg_catalog"."default",
  "priority_" int4,
  "due_date_" timestamp(6),
  "form_key_" varchar(255) COLLATE "pg_catalog"."default",
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "last_updated_time_" timestamp(6)
)
;
ALTER TABLE "public"."act_hi_taskinst" OWNER TO "root";

-- ----------------------------
-- Records of act_hi_taskinst
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_hi_taskinst" ("id_", "rev_", "proc_def_id_", "task_def_id_", "task_def_key_", "proc_inst_id_", "execution_id_", "scope_id_", "sub_scope_id_", "scope_type_", "scope_definition_id_", "propagated_stage_inst_id_", "name_", "parent_task_id_", "description_", "owner_", "assignee_", "start_time_", "claim_time_", "end_time_", "duration_", "delete_reason_", "priority_", "due_date_", "form_key_", "category_", "tenant_id_", "last_updated_time_") VALUES ('0d2f167e-c893-11ec-8a1d-3e2374911326', 2, 'oa_leave:1:934362e3-c892-11ec-a15b-3e2374911326', NULL, 'task01', '0d23cbd6-c893-11ec-8a1d-3e2374911326', '0d28119a-c893-11ec-8a1d-3e2374911326', NULL, NULL, NULL, NULL, NULL, 'task01', NULL, NULL, NULL, '1', '2022-04-30 22:37:24.822', NULL, '2022-04-30 22:40:30.345', 185523, NULL, 50, NULL, NULL, NULL, '', '2022-04-30 22:40:30.345');
COMMIT;

-- ----------------------------
-- Table structure for act_hi_tsk_log
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_tsk_log";
CREATE TABLE "public"."act_hi_tsk_log" (
  "id_" int4 NOT NULL DEFAULT nextval('act_hi_tsk_log_id__seq'::regclass),
  "type_" varchar(64) COLLATE "pg_catalog"."default",
  "task_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "time_stamp_" timestamp(6) NOT NULL,
  "user_id_" varchar(255) COLLATE "pg_catalog"."default",
  "data_" varchar(4000) COLLATE "pg_catalog"."default",
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_hi_tsk_log" OWNER TO "root";

-- ----------------------------
-- Records of act_hi_tsk_log
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_hi_varinst
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_hi_varinst";
CREATE TABLE "public"."act_hi_varinst" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4 DEFAULT 1,
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "name_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "var_type_" varchar(100) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "bytearray_id_" varchar(64) COLLATE "pg_catalog"."default",
  "double_" float8,
  "long_" int8,
  "text_" varchar(4000) COLLATE "pg_catalog"."default",
  "text2_" varchar(4000) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6),
  "last_updated_time_" timestamp(6)
)
;
ALTER TABLE "public"."act_hi_varinst" OWNER TO "root";

-- ----------------------------
-- Records of act_hi_varinst
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_hi_varinst" ("id_", "rev_", "proc_inst_id_", "execution_id_", "task_id_", "name_", "var_type_", "scope_id_", "sub_scope_id_", "scope_type_", "bytearray_id_", "double_", "long_", "text_", "text2_", "create_time_", "last_updated_time_") VALUES ('0d274e48-c893-11ec-8a1d-3e2374911326', 0, '0d23cbd6-c893-11ec-8a1d-3e2374911326', '0d23cbd6-c893-11ec-8a1d-3e2374911326', NULL, 'day', 'long', NULL, NULL, NULL, NULL, NULL, 15, '15', NULL, '2022-04-30 22:37:24.811', '2022-04-30 22:37:24.811');
COMMIT;

-- ----------------------------
-- Table structure for act_id_bytearray
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_id_bytearray";
CREATE TABLE "public"."act_id_bytearray" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "bytes_" bytea
)
;
ALTER TABLE "public"."act_id_bytearray" OWNER TO "root";

-- ----------------------------
-- Records of act_id_bytearray
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_id_group
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_id_group";
CREATE TABLE "public"."act_id_group" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "type_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_id_group" OWNER TO "root";

-- ----------------------------
-- Records of act_id_group
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_id_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_id_info";
CREATE TABLE "public"."act_id_info" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "user_id_" varchar(64) COLLATE "pg_catalog"."default",
  "type_" varchar(64) COLLATE "pg_catalog"."default",
  "key_" varchar(255) COLLATE "pg_catalog"."default",
  "value_" varchar(255) COLLATE "pg_catalog"."default",
  "password_" bytea,
  "parent_id_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_id_info" OWNER TO "root";

-- ----------------------------
-- Records of act_id_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_id_membership
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_id_membership";
CREATE TABLE "public"."act_id_membership" (
  "user_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "group_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "public"."act_id_membership" OWNER TO "root";

-- ----------------------------
-- Records of act_id_membership
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_id_priv
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_id_priv";
CREATE TABLE "public"."act_id_priv" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL
)
;
ALTER TABLE "public"."act_id_priv" OWNER TO "root";

-- ----------------------------
-- Records of act_id_priv
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_id_priv_mapping
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_id_priv_mapping";
CREATE TABLE "public"."act_id_priv_mapping" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "priv_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "user_id_" varchar(255) COLLATE "pg_catalog"."default",
  "group_id_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_id_priv_mapping" OWNER TO "root";

-- ----------------------------
-- Records of act_id_priv_mapping
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_id_property
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_id_property";
CREATE TABLE "public"."act_id_property" (
  "name_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "value_" varchar(300) COLLATE "pg_catalog"."default",
  "rev_" int4
)
;
ALTER TABLE "public"."act_id_property" OWNER TO "root";

-- ----------------------------
-- Records of act_id_property
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_id_property" ("name_", "value_", "rev_") VALUES ('schema.version', '6.7.0.0', 1);
COMMIT;

-- ----------------------------
-- Table structure for act_id_token
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_id_token";
CREATE TABLE "public"."act_id_token" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "token_value_" varchar(255) COLLATE "pg_catalog"."default",
  "token_date_" timestamp(6),
  "ip_address_" varchar(255) COLLATE "pg_catalog"."default",
  "user_agent_" varchar(255) COLLATE "pg_catalog"."default",
  "user_id_" varchar(255) COLLATE "pg_catalog"."default",
  "token_data_" varchar(2000) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_id_token" OWNER TO "root";

-- ----------------------------
-- Records of act_id_token
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_id_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_id_user";
CREATE TABLE "public"."act_id_user" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "first_" varchar(255) COLLATE "pg_catalog"."default",
  "last_" varchar(255) COLLATE "pg_catalog"."default",
  "display_name_" varchar(255) COLLATE "pg_catalog"."default",
  "email_" varchar(255) COLLATE "pg_catalog"."default",
  "pwd_" varchar(255) COLLATE "pg_catalog"."default",
  "picture_id_" varchar(64) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_id_user" OWNER TO "root";

-- ----------------------------
-- Records of act_id_user
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_procdef_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_procdef_info";
CREATE TABLE "public"."act_procdef_info" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "info_json_id_" varchar(64) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_procdef_info" OWNER TO "root";

-- ----------------------------
-- Records of act_procdef_info
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_re_deployment
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_re_deployment";
CREATE TABLE "public"."act_re_deployment" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "key_" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "deploy_time_" timestamp(6),
  "derived_from_" varchar(64) COLLATE "pg_catalog"."default",
  "derived_from_root_" varchar(64) COLLATE "pg_catalog"."default",
  "parent_deployment_id_" varchar(255) COLLATE "pg_catalog"."default",
  "engine_version_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_re_deployment" OWNER TO "root";

-- ----------------------------
-- Records of act_re_deployment
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_re_deployment" ("id_", "name_", "category_", "key_", "tenant_id_", "deploy_time_", "derived_from_", "derived_from_root_", "parent_deployment_id_", "engine_version_") VALUES ('93335d50-c892-11ec-a15b-3e2374911326', 'OA 请假', '2', 'oa_leave', '', '2022-04-30 22:34:00.204', NULL, NULL, '93335d50-c892-11ec-a15b-3e2374911326', NULL);
COMMIT;

-- ----------------------------
-- Table structure for act_re_model
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_re_model";
CREATE TABLE "public"."act_re_model" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "key_" varchar(255) COLLATE "pg_catalog"."default",
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6),
  "last_update_time_" timestamp(6),
  "version_" int4,
  "meta_info_" varchar(4000) COLLATE "pg_catalog"."default",
  "deployment_id_" varchar(64) COLLATE "pg_catalog"."default",
  "editor_source_value_id_" varchar(64) COLLATE "pg_catalog"."default",
  "editor_source_extra_value_id_" varchar(64) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_re_model" OWNER TO "root";

-- ----------------------------
-- Records of act_re_model
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_re_model" ("id_", "rev_", "name_", "key_", "category_", "create_time_", "last_update_time_", "version_", "meta_info_", "deployment_id_", "editor_source_value_id_", "editor_source_extra_value_id_", "tenant_id_") VALUES ('2d3151fa-c892-11ec-a15b-3e2374911326', 6, 'OA 请假', 'oa_leave', '2', '2022-04-30 22:31:09.063', '2022-04-30 22:34:00.361', 1, '{"description":null,"formType":20,"formId":null,"formCustomCreatePath":"/bpm/oa/leave/create","formCustomViewPath":"/bpm/oa/leave/detail"}', '93335d50-c892-11ec-a15b-3e2374911326', '65288f6b-c892-11ec-a15b-3e2374911326', NULL, '');
COMMIT;

-- ----------------------------
-- Table structure for act_re_procdef
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_re_procdef";
CREATE TABLE "public"."act_re_procdef" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "key_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "version_" int4 NOT NULL,
  "deployment_id_" varchar(64) COLLATE "pg_catalog"."default",
  "resource_name_" varchar(4000) COLLATE "pg_catalog"."default",
  "dgrm_resource_name_" varchar(4000) COLLATE "pg_catalog"."default",
  "description_" varchar(4000) COLLATE "pg_catalog"."default",
  "has_start_form_key_" bool,
  "has_graphical_notation_" bool,
  "suspension_state_" int4,
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "derived_from_" varchar(64) COLLATE "pg_catalog"."default",
  "derived_from_root_" varchar(64) COLLATE "pg_catalog"."default",
  "derived_version_" int4 NOT NULL DEFAULT 0,
  "engine_version_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_re_procdef" OWNER TO "root";

-- ----------------------------
-- Records of act_re_procdef
-- ----------------------------
BEGIN;
INSERT INTO "public"."act_re_procdef" ("id_", "rev_", "category_", "name_", "key_", "version_", "deployment_id_", "resource_name_", "dgrm_resource_name_", "description_", "has_start_form_key_", "has_graphical_notation_", "suspension_state_", "tenant_id_", "derived_from_", "derived_from_root_", "derived_version_", "engine_version_") VALUES ('oa_leave:1:934362e3-c892-11ec-a15b-3e2374911326', 2, '2', 'OA 请假', 'oa_leave', 1, '93335d50-c892-11ec-a15b-3e2374911326', 'oa_leave.bpmn', 'oa_leave.oa_leave.png', NULL, 'f', 't', 1, '', NULL, NULL, 0, NULL);
COMMIT;

-- ----------------------------
-- Table structure for act_ru_actinst
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_actinst";
CREATE TABLE "public"."act_ru_actinst" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4 DEFAULT 1,
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "act_id_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "call_proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "act_name_" varchar(255) COLLATE "pg_catalog"."default",
  "act_type_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "assignee_" varchar(255) COLLATE "pg_catalog"."default",
  "start_time_" timestamp(6) NOT NULL,
  "end_time_" timestamp(6),
  "duration_" int8,
  "transaction_order_" int4,
  "delete_reason_" varchar(4000) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_ru_actinst" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_actinst
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_deadletter_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_deadletter_job";
CREATE TABLE "public"."act_ru_deadletter_job" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "type_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "exclusive_" bool,
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "process_instance_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "element_id_" varchar(255) COLLATE "pg_catalog"."default",
  "element_name_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "correlation_id_" varchar(255) COLLATE "pg_catalog"."default",
  "exception_stack_id_" varchar(64) COLLATE "pg_catalog"."default",
  "exception_msg_" varchar(4000) COLLATE "pg_catalog"."default",
  "duedate_" timestamp(6),
  "repeat_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_type_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_cfg_" varchar(4000) COLLATE "pg_catalog"."default",
  "custom_values_id_" varchar(64) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6),
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_ru_deadletter_job" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_deadletter_job
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_entitylink
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_entitylink";
CREATE TABLE "public"."act_ru_entitylink" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "create_time_" timestamp(6),
  "link_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "parent_element_id_" varchar(255) COLLATE "pg_catalog"."default",
  "ref_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "ref_scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "ref_scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "root_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "root_scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "hierarchy_type_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_ru_entitylink" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_entitylink
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_event_subscr
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_event_subscr";
CREATE TABLE "public"."act_ru_event_subscr" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "event_type_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "event_name_" varchar(255) COLLATE "pg_catalog"."default",
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "activity_id_" varchar(64) COLLATE "pg_catalog"."default",
  "configuration_" varchar(255) COLLATE "pg_catalog"."default",
  "created_" timestamp(6) NOT NULL,
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(64) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(64) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(64) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(64) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_ru_event_subscr" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_event_subscr
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_execution
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_execution";
CREATE TABLE "public"."act_ru_execution" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "business_key_" varchar(255) COLLATE "pg_catalog"."default",
  "parent_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "super_exec_" varchar(64) COLLATE "pg_catalog"."default",
  "root_proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "act_id_" varchar(255) COLLATE "pg_catalog"."default",
  "is_active_" bool,
  "is_concurrent_" bool,
  "is_scope_" bool,
  "is_event_scope_" bool,
  "is_mi_root_" bool,
  "suspension_state_" int4,
  "cached_ent_state_" int4,
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "start_act_id_" varchar(255) COLLATE "pg_catalog"."default",
  "start_time_" timestamp(6),
  "start_user_id_" varchar(255) COLLATE "pg_catalog"."default",
  "lock_time_" timestamp(6),
  "lock_owner_" varchar(255) COLLATE "pg_catalog"."default",
  "is_count_enabled_" bool,
  "evt_subscr_count_" int4,
  "task_count_" int4,
  "job_count_" int4,
  "timer_job_count_" int4,
  "susp_job_count_" int4,
  "deadletter_job_count_" int4,
  "external_worker_job_count_" int4,
  "var_count_" int4,
  "id_link_count_" int4,
  "callback_id_" varchar(255) COLLATE "pg_catalog"."default",
  "callback_type_" varchar(255) COLLATE "pg_catalog"."default",
  "reference_id_" varchar(255) COLLATE "pg_catalog"."default",
  "reference_type_" varchar(255) COLLATE "pg_catalog"."default",
  "propagated_stage_inst_id_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_ru_execution" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_execution
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_external_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_external_job";
CREATE TABLE "public"."act_ru_external_job" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "type_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "lock_exp_time_" timestamp(6),
  "lock_owner_" varchar(255) COLLATE "pg_catalog"."default",
  "exclusive_" bool,
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "process_instance_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "element_id_" varchar(255) COLLATE "pg_catalog"."default",
  "element_name_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "correlation_id_" varchar(255) COLLATE "pg_catalog"."default",
  "retries_" int4,
  "exception_stack_id_" varchar(64) COLLATE "pg_catalog"."default",
  "exception_msg_" varchar(4000) COLLATE "pg_catalog"."default",
  "duedate_" timestamp(6),
  "repeat_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_type_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_cfg_" varchar(4000) COLLATE "pg_catalog"."default",
  "custom_values_id_" varchar(64) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6),
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_ru_external_job" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_external_job
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_history_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_history_job";
CREATE TABLE "public"."act_ru_history_job" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "lock_exp_time_" timestamp(6),
  "lock_owner_" varchar(255) COLLATE "pg_catalog"."default",
  "retries_" int4,
  "exception_stack_id_" varchar(64) COLLATE "pg_catalog"."default",
  "exception_msg_" varchar(4000) COLLATE "pg_catalog"."default",
  "handler_type_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_cfg_" varchar(4000) COLLATE "pg_catalog"."default",
  "custom_values_id_" varchar(64) COLLATE "pg_catalog"."default",
  "adv_handler_cfg_id_" varchar(64) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6),
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_ru_history_job" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_history_job
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_identitylink
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_identitylink";
CREATE TABLE "public"."act_ru_identitylink" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "group_id_" varchar(255) COLLATE "pg_catalog"."default",
  "type_" varchar(255) COLLATE "pg_catalog"."default",
  "user_id_" varchar(255) COLLATE "pg_catalog"."default",
  "task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_ru_identitylink" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_identitylink
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_job";
CREATE TABLE "public"."act_ru_job" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "type_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "lock_exp_time_" timestamp(6),
  "lock_owner_" varchar(255) COLLATE "pg_catalog"."default",
  "exclusive_" bool,
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "process_instance_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "element_id_" varchar(255) COLLATE "pg_catalog"."default",
  "element_name_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "correlation_id_" varchar(255) COLLATE "pg_catalog"."default",
  "retries_" int4,
  "exception_stack_id_" varchar(64) COLLATE "pg_catalog"."default",
  "exception_msg_" varchar(4000) COLLATE "pg_catalog"."default",
  "duedate_" timestamp(6),
  "repeat_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_type_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_cfg_" varchar(4000) COLLATE "pg_catalog"."default",
  "custom_values_id_" varchar(64) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6),
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_ru_job" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_job
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_suspended_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_suspended_job";
CREATE TABLE "public"."act_ru_suspended_job" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "type_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "exclusive_" bool,
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "process_instance_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "element_id_" varchar(255) COLLATE "pg_catalog"."default",
  "element_name_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "correlation_id_" varchar(255) COLLATE "pg_catalog"."default",
  "retries_" int4,
  "exception_stack_id_" varchar(64) COLLATE "pg_catalog"."default",
  "exception_msg_" varchar(4000) COLLATE "pg_catalog"."default",
  "duedate_" timestamp(6),
  "repeat_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_type_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_cfg_" varchar(4000) COLLATE "pg_catalog"."default",
  "custom_values_id_" varchar(64) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6),
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_ru_suspended_job" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_suspended_job
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_task
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_task";
CREATE TABLE "public"."act_ru_task" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "task_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "propagated_stage_inst_id_" varchar(255) COLLATE "pg_catalog"."default",
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "parent_task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "description_" varchar(4000) COLLATE "pg_catalog"."default",
  "task_def_key_" varchar(255) COLLATE "pg_catalog"."default",
  "owner_" varchar(255) COLLATE "pg_catalog"."default",
  "assignee_" varchar(255) COLLATE "pg_catalog"."default",
  "delegation_" varchar(64) COLLATE "pg_catalog"."default",
  "priority_" int4,
  "create_time_" timestamp(6),
  "due_date_" timestamp(6),
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "suspension_state_" int4,
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying,
  "form_key_" varchar(255) COLLATE "pg_catalog"."default",
  "claim_time_" timestamp(6),
  "is_count_enabled_" bool,
  "var_count_" int4,
  "id_link_count_" int4,
  "sub_task_count_" int4
)
;
ALTER TABLE "public"."act_ru_task" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_task
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_timer_job
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_timer_job";
CREATE TABLE "public"."act_ru_timer_job" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "type_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "lock_exp_time_" timestamp(6),
  "lock_owner_" varchar(255) COLLATE "pg_catalog"."default",
  "exclusive_" bool,
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "process_instance_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_def_id_" varchar(64) COLLATE "pg_catalog"."default",
  "element_id_" varchar(255) COLLATE "pg_catalog"."default",
  "element_name_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_definition_id_" varchar(255) COLLATE "pg_catalog"."default",
  "correlation_id_" varchar(255) COLLATE "pg_catalog"."default",
  "retries_" int4,
  "exception_stack_id_" varchar(64) COLLATE "pg_catalog"."default",
  "exception_msg_" varchar(4000) COLLATE "pg_catalog"."default",
  "duedate_" timestamp(6),
  "repeat_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_type_" varchar(255) COLLATE "pg_catalog"."default",
  "handler_cfg_" varchar(4000) COLLATE "pg_catalog"."default",
  "custom_values_id_" varchar(64) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6),
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."act_ru_timer_job" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_timer_job
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for act_ru_variable
-- ----------------------------
DROP TABLE IF EXISTS "public"."act_ru_variable";
CREATE TABLE "public"."act_ru_variable" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "type_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "execution_id_" varchar(64) COLLATE "pg_catalog"."default",
  "proc_inst_id_" varchar(64) COLLATE "pg_catalog"."default",
  "task_id_" varchar(64) COLLATE "pg_catalog"."default",
  "scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(255) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(255) COLLATE "pg_catalog"."default",
  "bytearray_id_" varchar(64) COLLATE "pg_catalog"."default",
  "double_" float8,
  "long_" int8,
  "text_" varchar(4000) COLLATE "pg_catalog"."default",
  "text2_" varchar(4000) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."act_ru_variable" OWNER TO "root";

-- ----------------------------
-- Records of act_ru_variable
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for flw_channel_definition
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_channel_definition";
CREATE TABLE "public"."flw_channel_definition" (
  "id_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "version_" int4,
  "key_" varchar(255) COLLATE "pg_catalog"."default",
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "deployment_id_" varchar(255) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(3),
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default",
  "resource_name_" varchar(255) COLLATE "pg_catalog"."default",
  "description_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."flw_channel_definition" OWNER TO "root";

-- ----------------------------
-- Records of flw_channel_definition
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for flw_ev_databasechangelog
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_ev_databasechangelog";
CREATE TABLE "public"."flw_ev_databasechangelog" (
  "id" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "author" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "filename" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "dateexecuted" timestamp(6) NOT NULL,
  "orderexecuted" int4 NOT NULL,
  "exectype" varchar(10) COLLATE "pg_catalog"."default" NOT NULL,
  "md5sum" varchar(35) COLLATE "pg_catalog"."default",
  "description" varchar(255) COLLATE "pg_catalog"."default",
  "comments" varchar(255) COLLATE "pg_catalog"."default",
  "tag" varchar(255) COLLATE "pg_catalog"."default",
  "liquibase" varchar(20) COLLATE "pg_catalog"."default",
  "contexts" varchar(255) COLLATE "pg_catalog"."default",
  "labels" varchar(255) COLLATE "pg_catalog"."default",
  "deployment_id" varchar(10) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."flw_ev_databasechangelog" OWNER TO "root";

-- ----------------------------
-- Records of flw_ev_databasechangelog
-- ----------------------------
BEGIN;
INSERT INTO "public"."flw_ev_databasechangelog" ("id", "author", "filename", "dateexecuted", "orderexecuted", "exectype", "md5sum", "description", "comments", "tag", "liquibase", "contexts", "labels", "deployment_id") VALUES ('1', 'flowable', 'org/flowable/eventregistry/db/liquibase/flowable-eventregistry-db-changelog.xml', '2022-04-30 21:26:56.159032', 1, 'EXECUTED', '8:1b0c48c9cf7945be799d868a2626d687', 'createTable tableName=FLW_EVENT_DEPLOYMENT; createTable tableName=FLW_EVENT_RESOURCE; createTable tableName=FLW_EVENT_DEFINITION; createIndex indexName=ACT_IDX_EVENT_DEF_UNIQ, tableName=FLW_EVENT_DEFINITION; createTable tableName=FLW_CHANNEL_DEFIN...', '', NULL, '4.3.5', NULL, NULL, '1325216030');
COMMIT;

-- ----------------------------
-- Table structure for flw_ev_databasechangeloglock
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_ev_databasechangeloglock";
CREATE TABLE "public"."flw_ev_databasechangeloglock" (
  "id" int4 NOT NULL,
  "locked" bool NOT NULL,
  "lockgranted" timestamp(6),
  "lockedby" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."flw_ev_databasechangeloglock" OWNER TO "root";

-- ----------------------------
-- Records of flw_ev_databasechangeloglock
-- ----------------------------
BEGIN;
INSERT INTO "public"."flw_ev_databasechangeloglock" ("id", "locked", "lockgranted", "lockedby") VALUES (1, 'f', NULL, NULL);
COMMIT;

-- ----------------------------
-- Table structure for flw_event_definition
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_event_definition";
CREATE TABLE "public"."flw_event_definition" (
  "id_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "version_" int4,
  "key_" varchar(255) COLLATE "pg_catalog"."default",
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "deployment_id_" varchar(255) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default",
  "resource_name_" varchar(255) COLLATE "pg_catalog"."default",
  "description_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."flw_event_definition" OWNER TO "root";

-- ----------------------------
-- Records of flw_event_definition
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for flw_event_deployment
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_event_deployment";
CREATE TABLE "public"."flw_event_deployment" (
  "id_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "category_" varchar(255) COLLATE "pg_catalog"."default",
  "deploy_time_" timestamp(3),
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default",
  "parent_deployment_id_" varchar(255) COLLATE "pg_catalog"."default"
)
;
ALTER TABLE "public"."flw_event_deployment" OWNER TO "root";

-- ----------------------------
-- Records of flw_event_deployment
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for flw_event_resource
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_event_resource";
CREATE TABLE "public"."flw_event_resource" (
  "id_" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "name_" varchar(255) COLLATE "pg_catalog"."default",
  "deployment_id_" varchar(255) COLLATE "pg_catalog"."default",
  "resource_bytes_" bytea
)
;
ALTER TABLE "public"."flw_event_resource" OWNER TO "root";

-- ----------------------------
-- Records of flw_event_resource
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for flw_ru_batch
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_ru_batch";
CREATE TABLE "public"."flw_ru_batch" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "type_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "search_key_" varchar(255) COLLATE "pg_catalog"."default",
  "search_key2_" varchar(255) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6) NOT NULL,
  "complete_time_" timestamp(6),
  "status_" varchar(255) COLLATE "pg_catalog"."default",
  "batch_doc_id_" varchar(64) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."flw_ru_batch" OWNER TO "root";

-- ----------------------------
-- Records of flw_ru_batch
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Table structure for flw_ru_batch_part
-- ----------------------------
DROP TABLE IF EXISTS "public"."flw_ru_batch_part";
CREATE TABLE "public"."flw_ru_batch_part" (
  "id_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "rev_" int4,
  "batch_id_" varchar(64) COLLATE "pg_catalog"."default",
  "type_" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "scope_id_" varchar(64) COLLATE "pg_catalog"."default",
  "sub_scope_id_" varchar(64) COLLATE "pg_catalog"."default",
  "scope_type_" varchar(64) COLLATE "pg_catalog"."default",
  "search_key_" varchar(255) COLLATE "pg_catalog"."default",
  "search_key2_" varchar(255) COLLATE "pg_catalog"."default",
  "create_time_" timestamp(6) NOT NULL,
  "complete_time_" timestamp(6),
  "status_" varchar(255) COLLATE "pg_catalog"."default",
  "result_doc_id_" varchar(64) COLLATE "pg_catalog"."default",
  "tenant_id_" varchar(255) COLLATE "pg_catalog"."default" DEFAULT ''::character varying
)
;
ALTER TABLE "public"."flw_ru_batch_part" OWNER TO "root";

-- ----------------------------
-- Records of flw_ru_batch_part
-- ----------------------------
BEGIN;
COMMIT;

-- ----------------------------
-- Primary Key structure for table act_evt_log
-- ----------------------------
ALTER TABLE "public"."act_evt_log" ADD CONSTRAINT "act_evt_log_pkey" PRIMARY KEY ("log_nr_");

-- ----------------------------
-- Indexes structure for table act_ge_bytearray
-- ----------------------------
CREATE INDEX "act_idx_bytear_depl" ON "public"."act_ge_bytearray" USING btree (
  "deployment_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ge_bytearray
-- ----------------------------
ALTER TABLE "public"."act_ge_bytearray" ADD CONSTRAINT "act_ge_bytearray_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_ge_property
-- ----------------------------
ALTER TABLE "public"."act_ge_property" ADD CONSTRAINT "act_ge_property_pkey" PRIMARY KEY ("name_");

-- ----------------------------
-- Indexes structure for table act_hi_actinst
-- ----------------------------
CREATE INDEX "act_idx_hi_act_inst_end" ON "public"."act_hi_actinst" USING btree (
  "end_time_" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_act_inst_exec" ON "public"."act_hi_actinst" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "act_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_act_inst_procinst" ON "public"."act_hi_actinst" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "act_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_act_inst_start" ON "public"."act_hi_actinst" USING btree (
  "start_time_" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_hi_actinst
-- ----------------------------
ALTER TABLE "public"."act_hi_actinst" ADD CONSTRAINT "act_hi_actinst_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_hi_attachment
-- ----------------------------
ALTER TABLE "public"."act_hi_attachment" ADD CONSTRAINT "act_hi_attachment_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_hi_comment
-- ----------------------------
ALTER TABLE "public"."act_hi_comment" ADD CONSTRAINT "act_hi_comment_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_hi_detail
-- ----------------------------
CREATE INDEX "act_idx_hi_detail_act_inst" ON "public"."act_hi_detail" USING btree (
  "act_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_detail_name" ON "public"."act_hi_detail" USING btree (
  "name_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_detail_proc_inst" ON "public"."act_hi_detail" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_detail_task_id" ON "public"."act_hi_detail" USING btree (
  "task_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_detail_time" ON "public"."act_hi_detail" USING btree (
  "time_" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_hi_detail
-- ----------------------------
ALTER TABLE "public"."act_hi_detail" ADD CONSTRAINT "act_hi_detail_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_hi_entitylink
-- ----------------------------
CREATE INDEX "act_idx_hi_ent_lnk_ref_scope" ON "public"."act_hi_entitylink" USING btree (
  "ref_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "ref_scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "link_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_ent_lnk_root_scope" ON "public"."act_hi_entitylink" USING btree (
  "root_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "root_scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "link_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_ent_lnk_scope" ON "public"."act_hi_entitylink" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "link_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_ent_lnk_scope_def" ON "public"."act_hi_entitylink" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "link_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_hi_entitylink
-- ----------------------------
ALTER TABLE "public"."act_hi_entitylink" ADD CONSTRAINT "act_hi_entitylink_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_hi_identitylink
-- ----------------------------
CREATE INDEX "act_idx_hi_ident_lnk_procinst" ON "public"."act_hi_identitylink" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_ident_lnk_scope" ON "public"."act_hi_identitylink" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_ident_lnk_scope_def" ON "public"."act_hi_identitylink" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_ident_lnk_sub_scope" ON "public"."act_hi_identitylink" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_ident_lnk_task" ON "public"."act_hi_identitylink" USING btree (
  "task_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_ident_lnk_user" ON "public"."act_hi_identitylink" USING btree (
  "user_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_hi_identitylink
-- ----------------------------
ALTER TABLE "public"."act_hi_identitylink" ADD CONSTRAINT "act_hi_identitylink_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_hi_procinst
-- ----------------------------
CREATE INDEX "act_idx_hi_pro_i_buskey" ON "public"."act_hi_procinst" USING btree (
  "business_key_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_pro_inst_end" ON "public"."act_hi_procinst" USING btree (
  "end_time_" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table act_hi_procinst
-- ----------------------------
ALTER TABLE "public"."act_hi_procinst" ADD CONSTRAINT "act_hi_procinst_proc_inst_id__key" UNIQUE ("proc_inst_id_");

-- ----------------------------
-- Primary Key structure for table act_hi_procinst
-- ----------------------------
ALTER TABLE "public"."act_hi_procinst" ADD CONSTRAINT "act_hi_procinst_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_hi_taskinst
-- ----------------------------
CREATE INDEX "act_idx_hi_task_inst_procinst" ON "public"."act_hi_taskinst" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_task_scope" ON "public"."act_hi_taskinst" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_task_scope_def" ON "public"."act_hi_taskinst" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_task_sub_scope" ON "public"."act_hi_taskinst" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_hi_taskinst
-- ----------------------------
ALTER TABLE "public"."act_hi_taskinst" ADD CONSTRAINT "act_hi_taskinst_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_hi_tsk_log
-- ----------------------------
ALTER TABLE "public"."act_hi_tsk_log" ADD CONSTRAINT "act_hi_tsk_log_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_hi_varinst
-- ----------------------------
CREATE INDEX "act_idx_hi_procvar_exe" ON "public"."act_hi_varinst" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_procvar_name_type" ON "public"."act_hi_varinst" USING btree (
  "name_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "var_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_procvar_proc_inst" ON "public"."act_hi_varinst" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_procvar_task_id" ON "public"."act_hi_varinst" USING btree (
  "task_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_var_scope_id_type" ON "public"."act_hi_varinst" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_hi_var_sub_id_type" ON "public"."act_hi_varinst" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_hi_varinst
-- ----------------------------
ALTER TABLE "public"."act_hi_varinst" ADD CONSTRAINT "act_hi_varinst_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_id_bytearray
-- ----------------------------
ALTER TABLE "public"."act_id_bytearray" ADD CONSTRAINT "act_id_bytearray_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_id_group
-- ----------------------------
ALTER TABLE "public"."act_id_group" ADD CONSTRAINT "act_id_group_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_id_info
-- ----------------------------
ALTER TABLE "public"."act_id_info" ADD CONSTRAINT "act_id_info_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_id_membership
-- ----------------------------
CREATE INDEX "act_idx_memb_group" ON "public"."act_id_membership" USING btree (
  "group_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_memb_user" ON "public"."act_id_membership" USING btree (
  "user_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_id_membership
-- ----------------------------
ALTER TABLE "public"."act_id_membership" ADD CONSTRAINT "act_id_membership_pkey" PRIMARY KEY ("user_id_", "group_id_");

-- ----------------------------
-- Uniques structure for table act_id_priv
-- ----------------------------
ALTER TABLE "public"."act_id_priv" ADD CONSTRAINT "act_uniq_priv_name" UNIQUE ("name_");

-- ----------------------------
-- Primary Key structure for table act_id_priv
-- ----------------------------
ALTER TABLE "public"."act_id_priv" ADD CONSTRAINT "act_id_priv_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_id_priv_mapping
-- ----------------------------
CREATE INDEX "act_idx_priv_group" ON "public"."act_id_priv_mapping" USING btree (
  "group_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_priv_mapping" ON "public"."act_id_priv_mapping" USING btree (
  "priv_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_priv_user" ON "public"."act_id_priv_mapping" USING btree (
  "user_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_id_priv_mapping
-- ----------------------------
ALTER TABLE "public"."act_id_priv_mapping" ADD CONSTRAINT "act_id_priv_mapping_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_id_property
-- ----------------------------
ALTER TABLE "public"."act_id_property" ADD CONSTRAINT "act_id_property_pkey" PRIMARY KEY ("name_");

-- ----------------------------
-- Primary Key structure for table act_id_token
-- ----------------------------
ALTER TABLE "public"."act_id_token" ADD CONSTRAINT "act_id_token_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_id_user
-- ----------------------------
ALTER TABLE "public"."act_id_user" ADD CONSTRAINT "act_id_user_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_procdef_info
-- ----------------------------
CREATE INDEX "act_idx_procdef_info_json" ON "public"."act_procdef_info" USING btree (
  "info_json_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_procdef_info_proc" ON "public"."act_procdef_info" USING btree (
  "proc_def_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Uniques structure for table act_procdef_info
-- ----------------------------
ALTER TABLE "public"."act_procdef_info" ADD CONSTRAINT "act_uniq_info_procdef" UNIQUE ("proc_def_id_");

-- ----------------------------
-- Primary Key structure for table act_procdef_info
-- ----------------------------
ALTER TABLE "public"."act_procdef_info" ADD CONSTRAINT "act_procdef_info_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_re_deployment
-- ----------------------------
ALTER TABLE "public"."act_re_deployment" ADD CONSTRAINT "act_re_deployment_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_re_model
-- ----------------------------
CREATE INDEX "act_idx_model_deployment" ON "public"."act_re_model" USING btree (
  "deployment_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_model_source" ON "public"."act_re_model" USING btree (
  "editor_source_value_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_model_source_extra" ON "public"."act_re_model" USING btree (
  "editor_source_extra_value_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_re_model
-- ----------------------------
ALTER TABLE "public"."act_re_model" ADD CONSTRAINT "act_re_model_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Uniques structure for table act_re_procdef
-- ----------------------------
ALTER TABLE "public"."act_re_procdef" ADD CONSTRAINT "act_uniq_procdef" UNIQUE ("key_", "version_", "derived_version_", "tenant_id_");

-- ----------------------------
-- Primary Key structure for table act_re_procdef
-- ----------------------------
ALTER TABLE "public"."act_re_procdef" ADD CONSTRAINT "act_re_procdef_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_actinst
-- ----------------------------
CREATE INDEX "act_idx_ru_acti_end" ON "public"."act_ru_actinst" USING btree (
  "end_time_" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ru_acti_exec" ON "public"."act_ru_actinst" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ru_acti_exec_act" ON "public"."act_ru_actinst" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "act_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ru_acti_proc" ON "public"."act_ru_actinst" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ru_acti_proc_act" ON "public"."act_ru_actinst" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "act_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ru_acti_start" ON "public"."act_ru_actinst" USING btree (
  "start_time_" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ru_acti_task" ON "public"."act_ru_actinst" USING btree (
  "task_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_actinst
-- ----------------------------
ALTER TABLE "public"."act_ru_actinst" ADD CONSTRAINT "act_ru_actinst_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_deadletter_job
-- ----------------------------
CREATE INDEX "act_idx_deadletter_job_correlation_id" ON "public"."act_ru_deadletter_job" USING btree (
  "correlation_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_deadletter_job_custom_values_id" ON "public"."act_ru_deadletter_job" USING btree (
  "custom_values_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_deadletter_job_exception_stack_id" ON "public"."act_ru_deadletter_job" USING btree (
  "exception_stack_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_deadletter_job_execution_id" ON "public"."act_ru_deadletter_job" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_deadletter_job_proc_def_id" ON "public"."act_ru_deadletter_job" USING btree (
  "proc_def_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_deadletter_job_process_instance_id" ON "public"."act_ru_deadletter_job" USING btree (
  "process_instance_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_djob_scope" ON "public"."act_ru_deadletter_job" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_djob_scope_def" ON "public"."act_ru_deadletter_job" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_djob_sub_scope" ON "public"."act_ru_deadletter_job" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_deadletter_job
-- ----------------------------
ALTER TABLE "public"."act_ru_deadletter_job" ADD CONSTRAINT "act_ru_deadletter_job_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_entitylink
-- ----------------------------
CREATE INDEX "act_idx_ent_lnk_ref_scope" ON "public"."act_ru_entitylink" USING btree (
  "ref_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "ref_scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "link_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ent_lnk_root_scope" ON "public"."act_ru_entitylink" USING btree (
  "root_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "root_scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "link_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ent_lnk_scope" ON "public"."act_ru_entitylink" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "link_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ent_lnk_scope_def" ON "public"."act_ru_entitylink" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "link_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_entitylink
-- ----------------------------
ALTER TABLE "public"."act_ru_entitylink" ADD CONSTRAINT "act_ru_entitylink_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_event_subscr
-- ----------------------------
CREATE INDEX "act_idx_event_subscr" ON "public"."act_ru_event_subscr" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_event_subscr_config_" ON "public"."act_ru_event_subscr" USING btree (
  "configuration_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_event_subscr
-- ----------------------------
ALTER TABLE "public"."act_ru_event_subscr" ADD CONSTRAINT "act_ru_event_subscr_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_execution
-- ----------------------------
CREATE INDEX "act_idx_exe_parent" ON "public"."act_ru_execution" USING btree (
  "parent_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_exe_procdef" ON "public"."act_ru_execution" USING btree (
  "proc_def_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_exe_procinst" ON "public"."act_ru_execution" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_exe_root" ON "public"."act_ru_execution" USING btree (
  "root_proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_exe_super" ON "public"."act_ru_execution" USING btree (
  "super_exec_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_exec_buskey" ON "public"."act_ru_execution" USING btree (
  "business_key_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_exec_ref_id_" ON "public"."act_ru_execution" USING btree (
  "reference_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_execution
-- ----------------------------
ALTER TABLE "public"."act_ru_execution" ADD CONSTRAINT "act_ru_execution_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_external_job
-- ----------------------------
CREATE INDEX "act_idx_ejob_scope" ON "public"."act_ru_external_job" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ejob_scope_def" ON "public"."act_ru_external_job" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ejob_sub_scope" ON "public"."act_ru_external_job" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_external_job_correlation_id" ON "public"."act_ru_external_job" USING btree (
  "correlation_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_external_job_custom_values_id" ON "public"."act_ru_external_job" USING btree (
  "custom_values_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_external_job_exception_stack_id" ON "public"."act_ru_external_job" USING btree (
  "exception_stack_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_external_job
-- ----------------------------
ALTER TABLE "public"."act_ru_external_job" ADD CONSTRAINT "act_ru_external_job_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table act_ru_history_job
-- ----------------------------
ALTER TABLE "public"."act_ru_history_job" ADD CONSTRAINT "act_ru_history_job_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_identitylink
-- ----------------------------
CREATE INDEX "act_idx_athrz_procedef" ON "public"."act_ru_identitylink" USING btree (
  "proc_def_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ident_lnk_group" ON "public"."act_ru_identitylink" USING btree (
  "group_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ident_lnk_scope" ON "public"."act_ru_identitylink" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ident_lnk_scope_def" ON "public"."act_ru_identitylink" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ident_lnk_sub_scope" ON "public"."act_ru_identitylink" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ident_lnk_user" ON "public"."act_ru_identitylink" USING btree (
  "user_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_idl_procinst" ON "public"."act_ru_identitylink" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_tskass_task" ON "public"."act_ru_identitylink" USING btree (
  "task_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_identitylink
-- ----------------------------
ALTER TABLE "public"."act_ru_identitylink" ADD CONSTRAINT "act_ru_identitylink_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_job
-- ----------------------------
CREATE INDEX "act_idx_job_correlation_id" ON "public"."act_ru_job" USING btree (
  "correlation_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_job_custom_values_id" ON "public"."act_ru_job" USING btree (
  "custom_values_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_job_exception_stack_id" ON "public"."act_ru_job" USING btree (
  "exception_stack_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_job_execution_id" ON "public"."act_ru_job" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_job_proc_def_id" ON "public"."act_ru_job" USING btree (
  "proc_def_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_job_process_instance_id" ON "public"."act_ru_job" USING btree (
  "process_instance_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_job_scope" ON "public"."act_ru_job" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_job_scope_def" ON "public"."act_ru_job" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_job_sub_scope" ON "public"."act_ru_job" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_job
-- ----------------------------
ALTER TABLE "public"."act_ru_job" ADD CONSTRAINT "act_ru_job_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_suspended_job
-- ----------------------------
CREATE INDEX "act_idx_sjob_scope" ON "public"."act_ru_suspended_job" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_sjob_scope_def" ON "public"."act_ru_suspended_job" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_sjob_sub_scope" ON "public"."act_ru_suspended_job" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_suspended_job_correlation_id" ON "public"."act_ru_suspended_job" USING btree (
  "correlation_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_suspended_job_custom_values_id" ON "public"."act_ru_suspended_job" USING btree (
  "custom_values_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_suspended_job_exception_stack_id" ON "public"."act_ru_suspended_job" USING btree (
  "exception_stack_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_suspended_job_execution_id" ON "public"."act_ru_suspended_job" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_suspended_job_proc_def_id" ON "public"."act_ru_suspended_job" USING btree (
  "proc_def_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_suspended_job_process_instance_id" ON "public"."act_ru_suspended_job" USING btree (
  "process_instance_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_suspended_job
-- ----------------------------
ALTER TABLE "public"."act_ru_suspended_job" ADD CONSTRAINT "act_ru_suspended_job_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_task
-- ----------------------------
CREATE INDEX "act_idx_task_create" ON "public"."act_ru_task" USING btree (
  "create_time_" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_task_exec" ON "public"."act_ru_task" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_task_procdef" ON "public"."act_ru_task" USING btree (
  "proc_def_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_task_procinst" ON "public"."act_ru_task" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_task_scope" ON "public"."act_ru_task" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_task_scope_def" ON "public"."act_ru_task" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_task_sub_scope" ON "public"."act_ru_task" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_task
-- ----------------------------
ALTER TABLE "public"."act_ru_task" ADD CONSTRAINT "act_ru_task_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_timer_job
-- ----------------------------
CREATE INDEX "act_idx_timer_job_correlation_id" ON "public"."act_ru_timer_job" USING btree (
  "correlation_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_timer_job_custom_values_id" ON "public"."act_ru_timer_job" USING btree (
  "custom_values_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_timer_job_duedate" ON "public"."act_ru_timer_job" USING btree (
  "duedate_" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_timer_job_exception_stack_id" ON "public"."act_ru_timer_job" USING btree (
  "exception_stack_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_timer_job_execution_id" ON "public"."act_ru_timer_job" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_timer_job_proc_def_id" ON "public"."act_ru_timer_job" USING btree (
  "proc_def_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_timer_job_process_instance_id" ON "public"."act_ru_timer_job" USING btree (
  "process_instance_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_tjob_scope" ON "public"."act_ru_timer_job" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_tjob_scope_def" ON "public"."act_ru_timer_job" USING btree (
  "scope_definition_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_tjob_sub_scope" ON "public"."act_ru_timer_job" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_timer_job
-- ----------------------------
ALTER TABLE "public"."act_ru_timer_job" ADD CONSTRAINT "act_ru_timer_job_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table act_ru_variable
-- ----------------------------
CREATE INDEX "act_idx_ru_var_scope_id_type" ON "public"."act_ru_variable" USING btree (
  "scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_ru_var_sub_id_type" ON "public"."act_ru_variable" USING btree (
  "sub_scope_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "scope_type_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_var_bytearray" ON "public"."act_ru_variable" USING btree (
  "bytearray_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_var_exe" ON "public"."act_ru_variable" USING btree (
  "execution_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_var_procinst" ON "public"."act_ru_variable" USING btree (
  "proc_inst_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);
CREATE INDEX "act_idx_variable_task_id" ON "public"."act_ru_variable" USING btree (
  "task_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table act_ru_variable
-- ----------------------------
ALTER TABLE "public"."act_ru_variable" ADD CONSTRAINT "act_ru_variable_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table flw_channel_definition
-- ----------------------------
CREATE UNIQUE INDEX "act_idx_channel_def_uniq" ON "public"."flw_channel_definition" USING btree (
  "key_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "version_" "pg_catalog"."int4_ops" ASC NULLS LAST,
  "tenant_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table flw_channel_definition
-- ----------------------------
ALTER TABLE "public"."flw_channel_definition" ADD CONSTRAINT "flw_channel_definition_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table flw_ev_databasechangeloglock
-- ----------------------------
ALTER TABLE "public"."flw_ev_databasechangeloglock" ADD CONSTRAINT "flw_ev_databasechangeloglock_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table flw_event_definition
-- ----------------------------
CREATE UNIQUE INDEX "act_idx_event_def_uniq" ON "public"."flw_event_definition" USING btree (
  "key_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST,
  "version_" "pg_catalog"."int4_ops" ASC NULLS LAST,
  "tenant_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table flw_event_definition
-- ----------------------------
ALTER TABLE "public"."flw_event_definition" ADD CONSTRAINT "flw_event_definition_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table flw_event_deployment
-- ----------------------------
ALTER TABLE "public"."flw_event_deployment" ADD CONSTRAINT "flw_event_deployment_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table flw_event_resource
-- ----------------------------
ALTER TABLE "public"."flw_event_resource" ADD CONSTRAINT "flw_event_resource_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Primary Key structure for table flw_ru_batch
-- ----------------------------
ALTER TABLE "public"."flw_ru_batch" ADD CONSTRAINT "flw_ru_batch_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Indexes structure for table flw_ru_batch_part
-- ----------------------------
CREATE INDEX "flw_idx_batch_part" ON "public"."flw_ru_batch_part" USING btree (
  "batch_id_" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table flw_ru_batch_part
-- ----------------------------
ALTER TABLE "public"."flw_ru_batch_part" ADD CONSTRAINT "flw_ru_batch_part_pkey" PRIMARY KEY ("id_");

-- ----------------------------
-- Foreign Keys structure for table act_ge_bytearray
-- ----------------------------
ALTER TABLE "public"."act_ge_bytearray" ADD CONSTRAINT "act_fk_bytearr_depl" FOREIGN KEY ("deployment_id_") REFERENCES "public"."act_re_deployment" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_id_membership
-- ----------------------------
ALTER TABLE "public"."act_id_membership" ADD CONSTRAINT "act_fk_memb_group" FOREIGN KEY ("group_id_") REFERENCES "public"."act_id_group" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_id_membership" ADD CONSTRAINT "act_fk_memb_user" FOREIGN KEY ("user_id_") REFERENCES "public"."act_id_user" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_id_priv_mapping
-- ----------------------------
ALTER TABLE "public"."act_id_priv_mapping" ADD CONSTRAINT "act_fk_priv_mapping" FOREIGN KEY ("priv_id_") REFERENCES "public"."act_id_priv" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_procdef_info
-- ----------------------------
ALTER TABLE "public"."act_procdef_info" ADD CONSTRAINT "act_fk_info_json_ba" FOREIGN KEY ("info_json_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_procdef_info" ADD CONSTRAINT "act_fk_info_procdef" FOREIGN KEY ("proc_def_id_") REFERENCES "public"."act_re_procdef" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_re_model
-- ----------------------------
ALTER TABLE "public"."act_re_model" ADD CONSTRAINT "act_fk_model_deployment" FOREIGN KEY ("deployment_id_") REFERENCES "public"."act_re_deployment" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_re_model" ADD CONSTRAINT "act_fk_model_source" FOREIGN KEY ("editor_source_value_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_re_model" ADD CONSTRAINT "act_fk_model_source_extra" FOREIGN KEY ("editor_source_extra_value_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_ru_deadletter_job
-- ----------------------------
ALTER TABLE "public"."act_ru_deadletter_job" ADD CONSTRAINT "act_fk_deadletter_job_custom_values" FOREIGN KEY ("custom_values_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_deadletter_job" ADD CONSTRAINT "act_fk_deadletter_job_exception" FOREIGN KEY ("exception_stack_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_deadletter_job" ADD CONSTRAINT "act_fk_deadletter_job_execution" FOREIGN KEY ("execution_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_deadletter_job" ADD CONSTRAINT "act_fk_deadletter_job_proc_def" FOREIGN KEY ("proc_def_id_") REFERENCES "public"."act_re_procdef" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_deadletter_job" ADD CONSTRAINT "act_fk_deadletter_job_process_instance" FOREIGN KEY ("process_instance_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_ru_event_subscr
-- ----------------------------
ALTER TABLE "public"."act_ru_event_subscr" ADD CONSTRAINT "act_fk_event_exec" FOREIGN KEY ("execution_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_ru_execution
-- ----------------------------
ALTER TABLE "public"."act_ru_execution" ADD CONSTRAINT "act_fk_exe_parent" FOREIGN KEY ("parent_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_execution" ADD CONSTRAINT "act_fk_exe_procdef" FOREIGN KEY ("proc_def_id_") REFERENCES "public"."act_re_procdef" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_execution" ADD CONSTRAINT "act_fk_exe_procinst" FOREIGN KEY ("proc_inst_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_execution" ADD CONSTRAINT "act_fk_exe_super" FOREIGN KEY ("super_exec_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_ru_external_job
-- ----------------------------
ALTER TABLE "public"."act_ru_external_job" ADD CONSTRAINT "act_fk_external_job_custom_values" FOREIGN KEY ("custom_values_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_external_job" ADD CONSTRAINT "act_fk_external_job_exception" FOREIGN KEY ("exception_stack_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_ru_identitylink
-- ----------------------------
ALTER TABLE "public"."act_ru_identitylink" ADD CONSTRAINT "act_fk_athrz_procedef" FOREIGN KEY ("proc_def_id_") REFERENCES "public"."act_re_procdef" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_identitylink" ADD CONSTRAINT "act_fk_idl_procinst" FOREIGN KEY ("proc_inst_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_identitylink" ADD CONSTRAINT "act_fk_tskass_task" FOREIGN KEY ("task_id_") REFERENCES "public"."act_ru_task" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_ru_job
-- ----------------------------
ALTER TABLE "public"."act_ru_job" ADD CONSTRAINT "act_fk_job_custom_values" FOREIGN KEY ("custom_values_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_job" ADD CONSTRAINT "act_fk_job_exception" FOREIGN KEY ("exception_stack_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_job" ADD CONSTRAINT "act_fk_job_execution" FOREIGN KEY ("execution_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_job" ADD CONSTRAINT "act_fk_job_proc_def" FOREIGN KEY ("proc_def_id_") REFERENCES "public"."act_re_procdef" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_job" ADD CONSTRAINT "act_fk_job_process_instance" FOREIGN KEY ("process_instance_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_ru_suspended_job
-- ----------------------------
ALTER TABLE "public"."act_ru_suspended_job" ADD CONSTRAINT "act_fk_suspended_job_custom_values" FOREIGN KEY ("custom_values_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_suspended_job" ADD CONSTRAINT "act_fk_suspended_job_exception" FOREIGN KEY ("exception_stack_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_suspended_job" ADD CONSTRAINT "act_fk_suspended_job_execution" FOREIGN KEY ("execution_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_suspended_job" ADD CONSTRAINT "act_fk_suspended_job_proc_def" FOREIGN KEY ("proc_def_id_") REFERENCES "public"."act_re_procdef" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_suspended_job" ADD CONSTRAINT "act_fk_suspended_job_process_instance" FOREIGN KEY ("process_instance_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_ru_task
-- ----------------------------
ALTER TABLE "public"."act_ru_task" ADD CONSTRAINT "act_fk_task_exe" FOREIGN KEY ("execution_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_task" ADD CONSTRAINT "act_fk_task_procdef" FOREIGN KEY ("proc_def_id_") REFERENCES "public"."act_re_procdef" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_task" ADD CONSTRAINT "act_fk_task_procinst" FOREIGN KEY ("proc_inst_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_ru_timer_job
-- ----------------------------
ALTER TABLE "public"."act_ru_timer_job" ADD CONSTRAINT "act_fk_timer_job_custom_values" FOREIGN KEY ("custom_values_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_timer_job" ADD CONSTRAINT "act_fk_timer_job_exception" FOREIGN KEY ("exception_stack_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_timer_job" ADD CONSTRAINT "act_fk_timer_job_execution" FOREIGN KEY ("execution_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_timer_job" ADD CONSTRAINT "act_fk_timer_job_proc_def" FOREIGN KEY ("proc_def_id_") REFERENCES "public"."act_re_procdef" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_timer_job" ADD CONSTRAINT "act_fk_timer_job_process_instance" FOREIGN KEY ("process_instance_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table act_ru_variable
-- ----------------------------
ALTER TABLE "public"."act_ru_variable" ADD CONSTRAINT "act_fk_var_bytearray" FOREIGN KEY ("bytearray_id_") REFERENCES "public"."act_ge_bytearray" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_variable" ADD CONSTRAINT "act_fk_var_exe" FOREIGN KEY ("execution_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE "public"."act_ru_variable" ADD CONSTRAINT "act_fk_var_procinst" FOREIGN KEY ("proc_inst_id_") REFERENCES "public"."act_ru_execution" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;

-- ----------------------------
-- Foreign Keys structure for table flw_ru_batch_part
-- ----------------------------
ALTER TABLE "public"."flw_ru_batch_part" ADD CONSTRAINT "flw_fk_batch_part_parent" FOREIGN KEY ("batch_id_") REFERENCES "public"."flw_ru_batch" ("id_") ON DELETE NO ACTION ON UPDATE NO ACTION;
