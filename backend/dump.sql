--
-- PostgreSQL database dump
--

-- Dumped from database version 14.18 (Debian 14.18-1.pgdg110+1)
-- Dumped by pg_dump version 17.5 (Debian 17.5-1.pgdg110+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Data for Name: levels; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.levels (id, accent, difficulty, name, description, "orderLevel", "DialogsIdsOfLevel", is_active, created_at) FROM stdin;
1	1	A2	Nivel medio	awqrkqkwrqwr	1		t	2025-07-25 18:00:56.494109
2	2	C2	Nivel gigliolini	este nivel es muy italinini	2	\N	t	2025-07-28 00:30:15.857191
\.


--
-- Data for Name: dialogs; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.dialogs (id, level_id, name, difficulty, description, audio_url, is_active, created_at) FROM stdin;
3	1	Dialogo Segundo 	A2	Esta es la description del Segundo dialogo que increible tambien o siiiiiiiiiii	https://ejemplo.com/audio.mp3	t	2025-07-27 18:37:28.982012
4	2	Dialogo deividzini	A1	Este nivel es italianini		t	2025-07-28 00:30:59.820805
2	1	Primer dialogo 1	A1	Este es El primer dialogo que incredible	https://ejemplo.com/audio.mp3	t	2025-07-27 18:37:07.389661
\.


--
-- Data for Name: dialogparticipants; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.dialogparticipants (id, dialog_id, name, created_at) FROM stdin;
2	2	Pepito	2025-07-31 02:01:41.548197
3	2	Pepito Sandoval	2025-08-01 03:56:02.456885
4	2	Pepito si	2025-08-01 04:55:13.349031
\.


--
-- Data for Name: phrase; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.phrase (id, participant_id, audio_url, english_text, spanish_text, is_active, created_at) FROM stdin;
2	2	\N	Hellou	["Hola","Que tal"]	t	2025-07-31 02:09:48.145887
\.


--
-- Data for Name: phraseorder; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.phraseorder (id, dialog_id, phrase_id, "order") FROM stdin;
\.


--
-- Data for Name: word; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.word (id, english, spanish, phonetic, description, is_active) FROM stdin;
1	Bye	Adiós	Bai	Se usa para despedirse	t
\.


--
-- Data for Name: phrasewords; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.phrasewords (id, phrase_id, word_id, "order") FROM stdin;
4	2	1	2
\.


--
-- Data for Name: tests; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.tests (id, level_id, name, description, test_type, difficulty, is_active, created_at) FROM stdin;
\.


--
-- Data for Name: testquestions; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.testquestions (id, test_id, question_text, correct_answer, options, "orderLevel") FROM stdin;
\.


--
-- Data for Name: userphrasestandby; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.userphrasestandby (id, user_id, phrase_id, incorrect_attempts, added_at) FROM stdin;
\.


--
-- Data for Name: userprogress; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.userprogress (id, user_id, level_id, completed_dialogs, total_dialogs, test_score, is_level_completed, last_accessed) FROM stdin;
\.


--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: dsandoval
--

COPY public.users (id, name, email, password, provider, "providerId", preferences, current_level_id, created_at, role) FROM stdin;
6	Deivid Sandoval	deivid@gmail.com	$2a$10$RW9r7vDrx.HJz8hPbBgRauQTiRf09mV3TzcffgrQyo2ts5jtOd0ka	local	local-deivid@gmail.com	oscuro	2	2025-08-01 00:27:33.098649	ADMIN
\.


--
-- Name: dialogparticipants_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.dialogparticipants_id_seq', 4, true);


--
-- Name: dialogs_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.dialogs_id_seq', 4, true);


--
-- Name: levels_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.levels_id_seq', 2, true);


--
-- Name: phrase_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.phrase_id_seq', 2, true);


--
-- Name: phraseorder_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.phraseorder_id_seq', 1, false);


--
-- Name: phrasewords_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.phrasewords_id_seq', 4, true);


--
-- Name: testquestions_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.testquestions_id_seq', 1, false);


--
-- Name: tests_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.tests_id_seq', 1, false);


--
-- Name: userphrasestandby_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.userphrasestandby_id_seq', 1, false);


--
-- Name: userprogress_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.userprogress_id_seq', 1, false);


--
-- Name: users_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.users_id_seq', 9, true);


--
-- Name: word_id_seq; Type: SEQUENCE SET; Schema: public; Owner: dsandoval
--

SELECT pg_catalog.setval('public.word_id_seq', 2, true);


--
-- PostgreSQL database dump complete
--

