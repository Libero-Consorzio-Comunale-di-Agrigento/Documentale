package xmlpack;

/**
 * 
 */
public class InfoConnessione {
  private String alias;
  private String user;
  private String passwd;
  private String dsn;

  /**
   * Costruttore
   */
  public InfoConnessione() {
  }

  /**
   * Costruttore
   */
  public InfoConnessione(String alias, String dsn, String user, String passwd) {
    this.alias = alias;
    this.dsn = dsn;
    this.user = user;
    this.passwd = passwd;
  }

  
  public String getAlias()
  {
    return alias;
  }

  public void setAlias(String newAlias)
  {
    alias = newAlias;
  }

  public String getUser()
  {
    return user;
  }

  public void setUser(String newUser)
  {
    user = newUser;
  }

  public String getPasswd()
  {
    return passwd;
  }

  public void setPasswd(String newPasswd)
  {
    passwd = newPasswd;
  }

  public String getDsn()
  {
    return dsn;
  }

  public void setDsn(String newDsn)
  {
    dsn = newDsn;
  }
}