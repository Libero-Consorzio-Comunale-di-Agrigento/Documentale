# Refactor da fare

## Migliorare il calcolo dei Soggetti
* Prendere esempio (o anche copiare miseramente) quanto fatto su Sfera: il problema è che ad oggi il
calcolo dei soggetti non è tipizzato (usa delle mappe).
* Semplificare l'invocazione dei vari metodi di calcolo dei soggetti usando un sistema simile a quanto fa 
SpringBoot con i @Controller: valorizza i parametri in base a quelli che sono definiti nella funzione che quindi
non deve più avere una "firma" precisa

## Migliorare le notifiche
* Consentire la definizione di un oggetto "Notifica" con più o meno parametri e passare quello ai vari service che poi la inviano.
* migliorare il modo in cui sono annidati i vari *Dispatcher*, in particolare quelli che non implementano l'interfaccia NotificheDispatcher.

## Migliorare lo storico
Ad oggi non è tipizzato ed è poco chiaro come funziona. Bisogna semplificarne l'uso e chiarirne le regole.

## Refactor delle varie interfacce IQualcosa 
Fa schifo che si chiamino IQualcosa. Generalizzarne il nome.

## Rinominare il bean successHandler
Non gestisce solo i messaggi "di successo".

## Migliorare tutto l'invio delle notifiche alla JWorklist/SmartDesktop
Le classi che ci stanno li sono terribili, lunghe km, mischiate, un pasticcio.
Creare una o più classi per l'invio delle notifiche da SmartDesktop separandola da quella per JWorklist.

## Eliminare la classe GestoreTransazioneFirma
Praticamente è un wrapper di FirmaService

## Rinominare il bean integrazioneGdmService in integrazioneDocumentaleService
Oppure qualcosa del genere. GDM non è più il nostro documentale di riferimento perché usiamo la smart-doc

## Refactor delle icone: vanno usate quelle "standard" nel jar finmatica-static-resources
e vanno eliminate tutte quelle presenti su questo repository (a meno che non ce ne siano di nuove da aggiungere rispetto allo standard)

## Spostare il ribaltamento dei dati su GDM in un Hibernate Interceptor o Hibernate Listener
L'idea è di ribaltare i dati su gdm ad ogni modifica su AGSPR.
Comunque deve essere possibile ribaltare i dati in altri momenti, ma in questo modo rimuoveremmo un sacco di codice ad-hoc
