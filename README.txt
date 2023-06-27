#################################
# Handler obsługujący komunikację BPM-CMIS
#################################

https://www.ibm.com/docs/en/baw/19.x?topic=clients-registering-custom-jax-ws-handlers

Instrukcja ($WEBSPHERE_ROOT to ścieżka do zainstalowanego WebSphere aplikacji IBM BPM np. /opt/IBM/BAW/21.x/):
1.	Zrobić backup konfiguracji dmgr i węzła
	a.	mkdir -p ~/workspace/backup/
	b.	cd $WEBSPHERE_ROOT/bin/
	c.	./manageprofiles.sh -backupProfile -profileName DmgrProfile -backupFile ~/workspace/backup/2023_DmgrProfile_backup.zip
	d.	./manageprofiles.sh -backupProfile -profileName Node1Profile -backupFile ~/workspace/backup/2023_Node1Profile_backup.zip
2.	(opcjonalnie, jężeli trzeba) Rozpakować archiwum ZIP z biblioteką
3.	Bibliotekę należy umieścić w katalogu $WEBSPHERE_ROOT/lib/ext
4.	Należy utworzyć plik konfiguracyjny w o nazwie cmis-repositories.properties w katalogu do którego ma dostęp użytkownik systemowy bpmadmin, np. w katalogu $WEBSPHERE_ROOT/properties. W pliku należy umieścić dane uwierzytelniania do repozytorium FileNet poprzez ustwienie dwóch parametrów, których nazwy budujemy następująco:
	a.	Parametr nazwy użytkownika: <nazwa_serwera_filenet>_<numer_portu>.username
	b.	Parametr nazwy użytkownika: <nazwa_serwera_filenet>_< numer_portu>.password
		Przykład:
		ecm-appdev1.xxx.com.pl_8080.username=POKHtest
		ecm-appdev1.xxx.com.pl_8080.password=POKHtest1234
	UWAGA! Numer portu jest obowiązkowy. Przypominam że dla komunikacji http numer portu to 80, a dla HTTPS 443
5.	Zmieniamy konfigurację serwera poprzez wydanie następujących 
	a.	cd $WEBSPHERE_ROOT/bin/
	b.	./wsadmin.sh -lang jython -conntype NONE
	c.	bpdServer = AdminConfig.getid("/Cell:/ServerCluster:<nazwa_klastra_aplikacji>/BPMClusterConfigExtension:/BPMProcessCenter:/")
	d.	techUserHandler0=AdminConfig.create('BPMCustomCmisHandler',bpdServer,[['className','pl.scisoftware.bpm.jaxws.ServerSOAPHandler'],['weight',0]],'cmisTechnicalUserJaxWsHandlers')
	e.	techProperty0=AdminConfig.create('BPMProperty',techUserHandler0,[['name','configDir'],['value','<pełna_ściezka_do_pliku_konfiguracji>']],'customProperties')
	f.	AdminConfig.save()
	g.	ssoHandler0=AdminConfig.create('BPMCustomCmisHandler',bpdServer,[['className','pl.scisoftware.bpm.jaxws.ServerSSOSOAPHandler'],['weight',0]],'cmisSsoJaxWsHandlers')
	h.	ssoProperty0=AdminConfig.create('BPMProperty',ssoHandler0,[['name','configDir'],['value','<pełna_ściezka_do_pliku_konfiguracji>']],'customProperties')
	i.	AdminConfig.save()
6.	W konsoli WebSphere (Services->Policy sets->General client policy set bindings)
7.	Wybieramy z listy pozycje BPM_SSO_CLIENT
8.	Wybieramy pozycję WS-Security->Custom properties
9.	Ustawiamy Outbound Custom Properties:
	com.ibm.wsspi.wssecurity.config.request.setMustUnderstand=false

Zmiany końcowe znajdą się w pliku:
profiles/Dmgr01/config/cells/PCDEVCell/clusters/BPMDEV.AppCluster/cluster-bpm.xml


--Skrypt--
cd /apps/IBM/WebSphere/ProcessCenter/bin/
./wsadmin.sh -lang jython -conntype NONE
bpdServer = AdminConfig.getid("/Cell:/ServerCluster:BPMDEV.AppCluster/BPMClusterConfigExtension:/BPMProcessCenter:/") 
techUserHandler0=AdminConfig.create('BPMCustomCmisHandler',bpdServer,[['className','pl.scisoftware.bpm.jaxws.ServerSOAPHandler'],['weight',0]],'cmisTechnicalUserJaxWsHandlers')
techProperty0=AdminConfig.create('BPMProperty',techUserHandler0,[['name','configDir'],['value','/apps/IBM/WebSphere/ProcessCenter/BPMCFG/configuration']],'customProperties')
AdminConfig.save()
bpdServer = AdminConfig.getid("/Cell:/ServerCluster:BPMDEV.AppCluster/BPMClusterConfigExtension:/BPMProcessCenter:/") 
ssoHandler0=AdminConfig.create('BPMCustomCmisHandler',bpdServer,[['className','pl.scisoftware.bpm.jaxws.ServerSSOSOAPHandler'],['weight',0]],'cmisSsoJaxWsHandlers')
ssoProperty0=AdminConfig.create('BPMProperty',ssoHandler0,[['name','configDir'],['value','/apps/IBM/WebSphere/ProcessCenter/BPMCFG/configuration']],'customProperties')
AdminConfig.save()
