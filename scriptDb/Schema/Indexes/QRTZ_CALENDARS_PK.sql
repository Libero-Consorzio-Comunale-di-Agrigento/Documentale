CREATE UNIQUE INDEX QRTZ_CALENDARS_PK ON QRTZ_CALENDARS
(SCHED_NAME, CALENDAR_NAME)
TABLESPACE GDM
PCTFREE    10
INITRANS   2
MAXTRANS   255
STORAGE    (
            MAXSIZE          UNLIMITED
            PCTINCREASE      0
            BUFFER_POOL      DEFAULT
           );


