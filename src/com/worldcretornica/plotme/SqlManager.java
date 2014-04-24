package com.worldcretornica.plotme;

import java.io.File;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import com.worldcretornica.plotme.utils.UUIDFetcher;

public class SqlManager {

    private static Connection conn = null;

    public final static String sqlitedb = "/plots.db";

    // todo add update to table for customprice, forsale

    private final static String PLOT_TABLE = "CREATE TABLE `plotmePlots` (" + 
            "`idX` INTEGER," + 
            "`idZ` INTEGER," + 
            "`owner` varchar(100) NOT NULL," + 
            "`world` varchar(32) NOT NULL DEFAULT '0'," + 
            "`topX` INTEGER NOT NULL DEFAULT '0'," + 
            "`bottomX` INTEGER NOT NULL DEFAULT '0'," + 
            "`topZ` INTEGER NOT NULL DEFAULT '0'," + 
            "`bottomZ` INTEGER NOT NULL DEFAULT '0'," + 
            "`biome` varchar(32) NOT NULL DEFAULT '0'," + 
            "`expireddate` DATETIME NULL," + 
            "`finished` boolean NOT NULL DEFAULT '0'," + 
            "`customprice` double NOT NULL DEFAULT '0'," + 
            "`forsale` boolean NOT NULL DEFAULT '0'," + 
            "`finisheddate` varchar(16) NULL," + 
            "`protected` boolean NOT NULL DEFAULT '0'," + 
            "`auctionned` boolean NOT NULL DEFAULT '0'," + 
            "`auctionenddate` varchar(16) NULL," + 
            "`currentbid` double NOT NULL DEFAULT '0'," + 
            "`currentbidder` varchar(32) NULL," + 
            "`currentbidderId` blob(16) NULL," + 
            "`ownerId` blob(16) NULL," + 
            "PRIMARY KEY (idX, idZ, world));";

    private final static String COMMENT_TABLE = "CREATE TABLE `plotmeComments` (" + 
            "`idX` INTEGER," + 
            "`idZ` INTEGER," + 
            "`world` varchar(32) NOT NULL," + 
            "`commentid` INTEGER," + 
            "`player` varchar(32) NOT NULL," +
            "`comment` text," + 
            "`playerid` blob(16) NOT NULL," + 
            "PRIMARY KEY (idX, idZ, world, commentid));";

    private final static String ALLOWED_TABLE = "CREATE TABLE `plotmeAllowed` (" + 
            "`idX` INTEGER," + 
            "`idZ` INTEGER," + 
            "`world` varchar(32) NOT NULL," + 
            "`player` varchar(32) NOT NULL," +
            "`playerid` blob(16) NOT NULL," +
            "PRIMARY KEY (idX, idZ, world, player));";

    private final static String DENIED_TABLE = "CREATE TABLE `plotmeDenied` (" + 
            "`idX` INTEGER," + 
            "`idZ` INTEGER," + 
            "`world` varchar(32) NOT NULL," + 
            "`player` varchar(32) NOT NULL," +
            "`playerid` blob(16) NOT NULL," +
            "PRIMARY KEY (idX, idZ, world, player));";

    public static Connection initialize() {
        try {
            if (PlotMe.usemySQL) {
                Class.forName("com.mysql.jdbc.Driver");
                conn = DriverManager.getConnection(PlotMe.mySQLconn, PlotMe.mySQLuname, PlotMe.mySQLpass);
                conn.setAutoCommit(false);
            } else {
                Class.forName("org.sqlite.JDBC");
                conn = DriverManager.getConnection("jdbc:sqlite:" + PlotMe.configpath + "/plots.db");
                conn.setAutoCommit(false);
            }
        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + "SQL exception on initialize :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            PlotMe.logger.severe(PlotMe.NAME + "You need the SQLite/MySQL library. :");
            PlotMe.logger.severe("  " + ex.getMessage());
        }

        createTable();

        return conn;
    }

    public static String getSchema() {
        String conn = PlotMe.mySQLconn;

        if (conn.lastIndexOf("/") > 0)
            return conn.substring(conn.lastIndexOf("/") + 1);
        else
            return "";
    }

    public static void UpdateTables() {
        Statement statement = null;
        ResultSet set = null;

        try {
            Connection conn = getConnection();

            statement = conn.createStatement();

            String schema = getSchema();

            if (PlotMe.usemySQL) {
                /*** START Version 0.8 changes ***/
                // CustomPrice
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmePlots' AND column_name='customprice'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmePlots ADD customprice double NOT NULL DEFAULT '0';");
                    conn.commit();
                }
                set.close();

                // ForSale
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmePlots' AND column_name='forsale'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmePlots ADD forsale boolean NOT NULL DEFAULT '0';");
                    conn.commit();
                }
                set.close();

                // finisheddate
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmePlots' AND column_name='finisheddate'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmePlots ADD finisheddate varchar(16) NULL;");
                    conn.commit();
                }
                set.close();

                // Protected
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmePlots' AND column_name='protected'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmePlots ADD protected boolean NOT NULL DEFAULT '0';");
                    conn.commit();
                }
                set.close();

                // Auctionned
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmePlots' AND column_name='auctionned'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmePlots ADD auctionned boolean NOT NULL DEFAULT '0';");
                    conn.commit();
                }
                set.close();

                // Auctionenddate
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmePlots' AND column_name='auctionenddate'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmePlots ADD auctionenddate varchar(16) NULL;");
                    conn.commit();
                }
                set.close();

                // Currentbidder
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmePlots' AND column_name='currentbidder'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmePlots ADD currentbidder varchar(32) NULL;");
                    conn.commit();
                }
                set.close();

                // Currentbid
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmePlots' AND column_name='currentbid'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmePlots ADD currentbid double NOT NULL DEFAULT '0';");
                    conn.commit();
                }
                set.close();

                /*** END Version 0.8 changes ***/

                /*** START Version 0.13d changes ***/

                // OwnerId
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmePlots' AND column_name='ownerid'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmePlots ADD ownerid blob(16) NULL;");
                    conn.commit();
                }
                set.close();

                // Allowed playerid
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmeAllowed' AND column_name='playerid'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmeAllowed ADD playerid blob(16) NULL;");
                    conn.commit();
                }
                set.close();

                // Denied playerid
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmeDenied' AND column_name='playerid'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmeDenied ADD playerid blob(16) NULL;");
                    conn.commit();
                }
                set.close();

                // Commenter playerid
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmeComments' AND column_name='playerid'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmeComments ADD playerid blob(16) NULL;");
                    conn.commit();
                }
                set.close();

                // CurrentBidderId
                set = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA = '" + schema + "' AND " + "TABLE_NAME='plotmePlots' AND column_name='currentbidderId'");
                if (!set.next()) {
                    statement.execute("ALTER TABLE plotmePlots ADD currentbidderId blob(16) NULL;");
                    conn.commit();
                }
                set.close();

                /*** END Version 0.13d changes ***/
            } else {
                String column;
                boolean found = false;

                /*** START Version 0.8 changes ***/
                // CustomPrice
                set = statement.executeQuery("PRAGMA table_info(`plotmePlots`)");
                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("customprice"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmePlots ADD customprice double NOT NULL DEFAULT '0';");
                    conn.commit();
                }
                set.close();
                found = false;

                // ForSale
                set = statement.executeQuery("PRAGMA table_info(`plotmePlots`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("forsale"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmePlots ADD forsale boolean NOT NULL DEFAULT '0';");
                    conn.commit();
                }
                set.close();
                found = false;

                // FinishedDate
                set = statement.executeQuery("PRAGMA table_info(`plotmePlots`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("finisheddate"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmePlots ADD finisheddate varchar(16) NULL;");
                    conn.commit();
                }
                set.close();
                found = false;

                // Protected
                set = statement.executeQuery("PRAGMA table_info(`plotmePlots`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("protected"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmePlots ADD protected boolean NOT NULL DEFAULT '0';");
                    conn.commit();
                }
                set.close();
                found = false;

                // Auctionned
                set = statement.executeQuery("PRAGMA table_info(`plotmePlots`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("auctionned"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmePlots ADD auctionned boolean NOT NULL DEFAULT '0';");
                    conn.commit();
                }
                set.close();
                found = false;

                // Auctionenddate
                set = statement.executeQuery("PRAGMA table_info(`plotmePlots`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("auctionenddate"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmePlots ADD auctionenddate varchar(16) NULL;");
                    conn.commit();
                }
                set.close();
                found = false;

                // Currentbidder
                set = statement.executeQuery("PRAGMA table_info(`plotmePlots`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("currentbidder"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmePlots ADD currentbidder varchar(32) NULL;");
                    conn.commit();
                }
                set.close();
                found = false;

                // Currentbid
                set = statement.executeQuery("PRAGMA table_info(`plotmePlots`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("currentbid"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmePlots ADD currentbid double NOT NULL DEFAULT '0';");
                    conn.commit();
                }
                set.close();
                found = false;
                /*** END Version 0.8 changes ***/

                /*** START Version 0.13d changes ***/

                // OwnerId
                set = statement.executeQuery("PRAGMA table_info(`plotmePlots`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("ownerid"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmePlots ADD ownerid blob(16) NULL;");
                    conn.commit();
                }
                set.close();
                found = false;

                // Allowed id
                set = statement.executeQuery("PRAGMA table_info(`plotmeAllowed`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("playerid"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmeAllowed ADD playerid blob(16) NULL;");
                    conn.commit();
                }
                set.close();
                found = false;

                // Denied id
                set = statement.executeQuery("PRAGMA table_info(`plotmeDenied`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("playerid"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmeDenied ADD playerid blob(16) NULL;");
                    conn.commit();
                }
                set.close();
                found = false;

                // Commenter id
                set = statement.executeQuery("PRAGMA table_info(`plotmeComments`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("playerid"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmeComments ADD playerid blob(16) NULL;");
                    conn.commit();
                }
                set.close();
                found = false;

                // CurrentBidderId
                set = statement.executeQuery("PRAGMA table_info(`plotmePlots`)");

                while (set.next() && !found) {
                    column = set.getString(2);
                    if (column.equalsIgnoreCase("currentbidderId"))
                        found = true;
                }

                if (!found) {
                    statement.execute("ALTER TABLE plotmePlots ADD currentbidderId blob(16) NULL;");
                    conn.commit();
                }
                set.close();
                found = false;

                /*** END Version 0.13d changes ***/

            }
        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Update table exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (set != null)
                    set.close();
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Update table exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    public static Connection getConnection() {
        if (conn == null)
            conn = initialize();
        if (PlotMe.usemySQL) {
            try {
                if (!conn.isValid(10))
                    conn = initialize();
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + "Failed to check SQL status :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
        return conn;
    }

    public static void closeConnection() {
        if (conn != null) {
            try {
                if (PlotMe.usemySQL) {
                    if (conn.isValid(10)) {
                        conn.close();
                    }
                    conn = null;
                } else {
                    conn.close();
                    conn = null;
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + "Error on Connection close :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    private static boolean tableExists(String name) {
        ResultSet rs = null;
        try {
            Connection conn = getConnection();

            DatabaseMetaData dbm = conn.getMetaData();
            rs = dbm.getTables(null, null, name, null);
            if (!rs.next())
                return false;
            return true;
        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Table Check Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
            return false;
        } finally {
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Table Check SQL Exception (on closing) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    private static void createTable() {
        Statement st = null;
        try {
            Connection conn = getConnection();
            st = conn.createStatement();

            if (!tableExists("plotmePlots")) {
                st.executeUpdate(PLOT_TABLE);
                conn.commit();
            }

            if (!tableExists("plotmeComments")) {
                st.executeUpdate(COMMENT_TABLE);
                conn.commit();
            }

            if (!tableExists("plotmeAllowed")) {
                st.executeUpdate(ALLOWED_TABLE);
                conn.commit();
            }

            if (!tableExists("plotmeDenied")) {
                st.executeUpdate(DENIED_TABLE);
                conn.commit();
            }

            UpdateTables();

            if (PlotMe.usemySQL) {
                File sqlitefile = new File(PlotMe.configpath + sqlitedb);
                if (!sqlitefile.exists()) {
                    return;
                } else {
                    PlotMe.logger.info(PlotMe.NAME + " Modifying database for MySQL support");
                    PlotMe.logger.info(PlotMe.NAME + " Trying to import plots from plots.db");
                    Class.forName("org.sqlite.JDBC");
                    Connection sqliteconn = DriverManager.getConnection("jdbc:sqlite:" + PlotMe.configpath + sqlitedb);
                    sqliteconn.setAutoCommit(false);
                    Statement slstatement = sqliteconn.createStatement();
                    ResultSet setPlots = slstatement.executeQuery("SELECT * FROM plotmePlots");
                    Statement slAllowed = sqliteconn.createStatement();
                    ResultSet setAllowed = null;
                    Statement slDenied = sqliteconn.createStatement();
                    ResultSet setDenied = null;
                    Statement slComments = sqliteconn.createStatement();
                    ResultSet setComments = null;

                    int size = 0;
                    while (setPlots.next()) {
                        int idX = setPlots.getInt("idX");
                        int idZ = setPlots.getInt("idZ");
                        String owner = setPlots.getString("owner");
                        String world = setPlots.getString("world").toLowerCase();
                        int topX = setPlots.getInt("topX");
                        int bottomX = setPlots.getInt("bottomX");
                        int topZ = setPlots.getInt("topZ");
                        int bottomZ = setPlots.getInt("bottomZ");
                        String biome = setPlots.getString("biome");
                        java.sql.Date expireddate = setPlots.getDate("expireddate");
                        boolean finished = setPlots.getBoolean("finished");
                        PlayerList allowed = new PlayerList();
                        PlayerList denied = new PlayerList();
                        List<String[]> comments = new ArrayList<String[]>();
                        double customprice = setPlots.getDouble("customprice");
                        boolean forsale = setPlots.getBoolean("forsale");
                        String finisheddate = setPlots.getString("finisheddate");
                        boolean protect = setPlots.getBoolean("protected");
                        boolean auctionned = setPlots.getBoolean("auctionned");
                        String currentbidder = setPlots.getString("currentbidder");
                        double currentbid = setPlots.getDouble("currentbid");

                        byte[] byOwner = setPlots.getBytes("ownerId");
                        byte[] byBidder = setPlots.getBytes("currentbidderid");

                        UUID ownerId = null;
                        UUID currentbidderid = null;

                        if (byOwner != null) {
                            ownerId = UUIDFetcher.fromBytes(byOwner);
                        }
                        if (byBidder != null) {
                            currentbidderid = UUIDFetcher.fromBytes(byBidder);
                        }

                        setAllowed = slAllowed.executeQuery("SELECT * FROM plotmeAllowed WHERE idX = '" + idX + "' AND idZ = '" + idZ + "' AND world = '" + world + "'");

                        while (setAllowed.next()) {
                            byte[] byPlayerId = setAllowed.getBytes("playerid");
                            if (byPlayerId == null) {
                                allowed.put(setAllowed.getString("player"));
                            } else {
                                allowed.put(setAllowed.getString("player"), UUIDFetcher.fromBytes(byPlayerId));
                            }
                        }

                        if (setAllowed != null) {
                            setAllowed.close();
                        }

                        setDenied = slDenied.executeQuery("SELECT * FROM plotmeDenied WHERE idX = '" + idX + "' AND idZ = '" + idZ + "' AND world = '" + world + "'");

                        while (setDenied.next()) {
                            byte[] byPlayerId = setDenied.getBytes("playerid");
                            if (byPlayerId == null) {
                                denied.put(setDenied.getString("player"));
                            } else {
                                denied.put(setDenied.getString("player"), UUIDFetcher.fromBytes(byPlayerId));
                            }
                        }

                        if (setDenied != null) {
                            setDenied.close();
                        }

                        setComments = slComments.executeQuery("SELECT * FROM plotmeComments WHERE idX = '" + idX + "' AND idZ = '" + idZ + "' AND world = '" + world + "'");

                        while (setComments.next()) {
                            String[] comment = new String[3];

                            byte[] byPlayerId = setDenied.getBytes("playerid");
                            if (byPlayerId != null) {
                                comment[2] = UUIDFetcher.fromBytes(byPlayerId).toString();
                            } else {
                                comment[2] = null;
                            }

                            comment[0] = setComments.getString("player");
                            comment[1] = setComments.getString("comment");
                            comments.add(comment);
                        }

                        Plot plot = new Plot(owner, ownerId, world, topX, bottomX, topZ, bottomZ, biome, expireddate, finished, allowed, comments, "" + idX + ";" + idZ, customprice, forsale, finisheddate, protect, currentbidder, currentbidderid, currentbid, auctionned, denied);
                        addPlot(plot, idX, idZ, topX, bottomX, topZ, bottomZ);

                        size++;
                    }
                    PlotMe.logger.info(PlotMe.NAME + " Imported " + size + " plots from " + sqlitedb);
                    if (slstatement != null)
                        slstatement.close();
                    if (slAllowed != null)
                        slAllowed.close();
                    if (slComments != null)
                        slComments.close();
                    if (slDenied != null)
                        slDenied.close();
                    if (setPlots != null)
                        setPlots.close();
                    if (setComments != null)
                        setComments.close();
                    if (setAllowed != null)
                        setAllowed.close();
                    if (sqliteconn != null)
                        sqliteconn.close();

                    PlotMe.logger.info(PlotMe.NAME + " Renaming " + sqlitedb + " to " + sqlitedb + ".old");
                    if (!sqlitefile.renameTo(new File(PlotMe.configpath, sqlitedb + ".old"))) {
                        PlotMe.logger.warning(PlotMe.NAME + " Failed to rename " + sqlitedb + "! Please rename this manually!");
                    }
                }
            }
        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Create Table Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } catch (ClassNotFoundException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " You need the SQLite library :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (st != null) {
                    st.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Could not create the table (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    public static void addPlot(Plot plot, int idX, int idZ, World w) {
        addPlot(plot, idX, idZ, PlotManager.topX(plot.id, w), PlotManager.bottomX(plot.id, w), PlotManager.topZ(plot.id, w), PlotManager.bottomZ(plot.id, w));
    }

    public static void addPlot(Plot plot, int idX, int idZ, int topX, int bottomX, int topZ, int bottomZ) {
        PreparedStatement ps = null;
        Connection conn;
        StringBuilder strSql = new StringBuilder();

        // Plots
        try {
            conn = getConnection();

            strSql.append("INSERT INTO plotmePlots (idX, idZ, owner, world, topX, bottomX, topZ, bottomZ, ");
            strSql.append("biome, expireddate, finished, customprice, forsale, finisheddate, protected,");
            strSql.append("auctionned, auctionenddate, currentbid, currentbidder, currentbidderId, ownerId) ");
            strSql.append("VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");

            ps = conn.prepareStatement(strSql.toString());
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, plot.owner);
            ps.setString(4, plot.world.toLowerCase());
            ps.setInt(5, topX);
            ps.setInt(6, bottomX);
            ps.setInt(7, topZ);
            ps.setInt(8, bottomZ);
            ps.setString(9, plot.biome.name());
            ps.setDate(10, plot.expireddate);
            ps.setBoolean(11, plot.finished);
            ps.setDouble(12, plot.customprice);
            ps.setBoolean(13, plot.forsale);
            ps.setString(14, plot.finisheddate);
            ps.setBoolean(15, plot.protect);
            ps.setBoolean(16, plot.auctionned);
            ps.setDouble(17, plot.currentbid);
            ps.setString(18, plot.currentbidder);
            if (plot.currentbidderId != null) {
                ps.setBlob(19, fromUUIDToBlob(plot.currentbidderId));
            } else {
                ps.setNull(19, java.sql.Types.BLOB);
            }
            if (plot.ownerId != null) {
                ps.setBlob(20, fromUUIDToBlob(plot.ownerId));
            } else {
                ps.setNull(20, java.sql.Types.BLOB);
            }

            ps.executeUpdate();
            conn.commit();

            if (!plot.owner.equals("") && plot.ownerId == null) {
                fetchOwnerUUIDAsync(idX, idZ, plot.world.toLowerCase(), plot.owner);
            }

            if (!plot.currentbidder.equals("") && plot.currentbidderId == null) {
                fetchBidderUUIDAsync(idX, idZ, plot.world.toLowerCase(), plot.currentbidder);
            }

        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Insert Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Insert Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    public static void updatePlot(int idX, int idZ, String world, String field, Object value) {
        PreparedStatement ps = null;
        Connection conn;

        // Plots
        try {
            conn = getConnection();

            ps = conn.prepareStatement("UPDATE plotmePlots SET " + field + " = ? " + "WHERE idX = ? AND idZ = ? AND world = ?");

            if (value instanceof UUID) {
                ps.setBlob(1, fromUUIDToBlob((UUID) value));
            } else {
                ps.setObject(1, value);
            }
            ps.setInt(2, idX);
            ps.setInt(3, idZ);
            ps.setString(4, world.toLowerCase());

            ps.executeUpdate();
            conn.commit();

            if (field.equalsIgnoreCase("owner")) {
                fetchOwnerUUIDAsync(idX, idZ, world, value.toString());
            } else if (field.equalsIgnoreCase("currentbidder")) {
                fetchBidderUUIDAsync(idX, idZ, world, value.toString());
            }

        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Insert Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Insert Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    public static void updateTable(String tablename, int idX, int idZ, String world, String field, Object value) {
        PreparedStatement ps = null;
        Connection conn;

        // Plots
        try {
            conn = getConnection();

            ps = conn.prepareStatement("UPDATE " + tablename + " SET " + field + " = ? " + "WHERE idX = ? AND idZ = ? AND world = ?");

            if (value instanceof UUID) {
                ps.setBlob(1, fromUUIDToBlob((UUID) value));
            } else {
                ps.setObject(1, value);
            }
            ps.setInt(2, idX);
            ps.setInt(3, idZ);
            ps.setString(4, world.toLowerCase());

            ps.executeUpdate();
            conn.commit();

            if (field.equalsIgnoreCase("owner")) {
                fetchOwnerUUIDAsync(idX, idZ, world, value.toString());
            } else if (field.equalsIgnoreCase("currentbidder")) {
                fetchBidderUUIDAsync(idX, idZ, world, value.toString());
            } else if (field.equalsIgnoreCase("player")) {
                if (tablename.equalsIgnoreCase("plotmeallowed")) {

                }
            }

        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Insert Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Insert Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    public static void addPlotAllowed(String player, int idX, int idZ, String world) {
        addPlotAllowed(player, null, idX, idZ, world);
    }

    public static void addPlotAllowed(String player, UUID playerid, int idX, int idZ, String world) {
        PreparedStatement ps = null;
        Connection conn;

        // Allowed
        try {
            conn = getConnection();

            ps = conn.prepareStatement("INSERT INTO plotmeAllowed (idX, idZ, player, world, playerid) " + "VALUES (?,?,?,?,?)");

            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, player);
            ps.setString(4, world.toLowerCase());
            if (playerid != null) {
                ps.setBlob(5, fromUUIDToBlob(playerid));
            } else {
                ps.setNull(5, java.sql.Types.BLOB);
            }

            ps.executeUpdate();
            conn.commit();

            if (playerid == null) {
                fetchAllowedUUIDAsync(idX, idZ, world, player);
            }

        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Insert Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Insert Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    @Deprecated
    public static void addPlotDenied(String player, int idX, int idZ, String world) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(player);
        if (op == null) {
            addPlotDenied(player, null, idX, idZ, world);
        } else {
            addPlotDenied(player, op.getUniqueId(), idX, idZ, world);
        }
    }

    public static void addPlotDenied(String player, UUID playerid, int idX, int idZ, String world) {
        PreparedStatement ps = null;
        Connection conn;

        // Denied
        try {
            conn = getConnection();

            ps = conn.prepareStatement("INSERT INTO plotmeDenied (idX, idZ, player, world, playerid) " + "VALUES (?,?,?,?,?)");

            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, player);
            ps.setString(4, world.toLowerCase());
            if (playerid != null) {
                ps.setBlob(5, fromUUIDToBlob(playerid));
            } else {
                ps.setNull(5, java.sql.Types.BLOB);
            }

            ps.executeUpdate();
            conn.commit();

            if (playerid == null) {
                fetchDeniedUUIDAsync(idX, idZ, world, player);
            }

        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Insert Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Insert Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    public static void addPlotComment(String[] comment, int commentid, int idX, int idZ, String world) {
        PreparedStatement ps = null;
        Connection conn;

        // Comments
        try {
            conn = getConnection();

            ps = conn.prepareStatement("INSERT INTO plotmeComments (idX, idZ, commentid, player, comment, world, playerid) " + "VALUES (?,?,?,?,?,?,?)");

            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setInt(3, commentid);
            ps.setString(4, comment[0]);
            ps.setString(5, comment[1]);
            ps.setString(6, world.toLowerCase());
            if (!comment[2].equals("")) {
                ps.setBlob(7, fromUUIDToBlob(UUIDFetcher.getUUID(comment[2])));
            } else {
                ps.setNull(7, java.sql.Types.BLOB);
            }

            ps.executeUpdate();
            conn.commit();

            if (comment[2].equals("")) {
                fetchCommenterUUIDAsync(idX, idZ, world, commentid, comment[0]);
            }

        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Insert Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Insert Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    public static void deletePlot(int idX, int idZ, String world) {
        PreparedStatement ps = null;
        ResultSet set = null;
        try {
            Connection conn = getConnection();

            ps = conn.prepareStatement("DELETE FROM plotmeComments WHERE idX = ? and idZ = ? and LOWER(world) = ?");
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, world);
            ps.executeUpdate();
            conn.commit();
            ps.close();

            ps = conn.prepareStatement("DELETE FROM plotmeAllowed WHERE idX = ? and idZ = ? and LOWER(world) = ?");
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, world);
            ps.executeUpdate();
            conn.commit();
            ps.close();

            ps = conn.prepareStatement("DELETE FROM plotmeDenied WHERE idX = ? and idZ = ? and LOWER(world) = ?");
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, world);
            ps.executeUpdate();
            conn.commit();
            ps.close();

            ps = conn.prepareStatement("DELETE FROM plotmePlots WHERE idX = ? and idZ = ? and LOWER(world) = ?");
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(3, world);
            ps.executeUpdate();
            conn.commit();
            ps.close();

        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Delete Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Delete Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    public static void deletePlotComment(int idX, int idZ, int commentid, String world) {
        PreparedStatement ps = null;
        ResultSet set = null;
        try {
            Connection conn = getConnection();

            ps = conn.prepareStatement("DELETE FROM plotmeComments WHERE idX = ? and idZ = ? and commentid = ? and LOWER(world) = ?");
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setInt(3, commentid);
            ps.setString(4, world);
            ps.executeUpdate();
            conn.commit();

        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Delete Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Delete Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    @Deprecated
    public static void deletePlotAllowed(int idX, int idZ, String player, String world) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(player);
        if (op == null) {
            deletePlotAllowed(idX, idZ, player, null, world);
        } else {
            deletePlotAllowed(idX, idZ, player, op.getUniqueId(), world);
        }
    }

    public static void deletePlotAllowed(int idX, int idZ, String player, UUID playerid, String world) {
        PreparedStatement ps = null;
        ResultSet set = null;

        try {
            Connection conn = getConnection();

            if (playerid == null) {
                ps = conn.prepareStatement("DELETE FROM plotmeAllowed WHERE idX = ? and idZ = ? and player = ? and LOWER(world) = ?");
                ps.setString(3, player);
            } else {
                ps = conn.prepareStatement("DELETE FROM plotmeAllowed WHERE idX = ? and idZ = ? and playerid = ? and LOWER(world) = ?");
                ps.setBlob(3, fromUUIDToBlob(playerid));
            }
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(4, world);
            ps.executeUpdate();
            conn.commit();

        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Delete Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Delete Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    @Deprecated
    public static void deletePlotDenied(int idX, int idZ, String player, String world) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(player);
        if (op == null) {
            deletePlotDenied(idX, idZ, player, null, world);
        } else {
            deletePlotDenied(idX, idZ, player, op.getUniqueId(), world);
        }
    }

    public static void deletePlotDenied(int idX, int idZ, String player, UUID playerid, String world) {
        PreparedStatement ps = null;
        ResultSet set = null;

        try {
            Connection conn = getConnection();

            if (playerid == null) {
                ps = conn.prepareStatement("DELETE FROM plotmeDenied WHERE idX = ? and idZ = ? and player = ? and LOWER(world) = ?");
                ps.setString(3, player);
            } else {
                ps = conn.prepareStatement("DELETE FROM plotmeDenied WHERE idX = ? and idZ = ? and playerid = ? and LOWER(world) = ?");
                ps.setBlob(3, fromUUIDToBlob(playerid));
            }
            ps.setInt(1, idX);
            ps.setInt(2, idZ);
            ps.setString(4, world);
            ps.executeUpdate();
            conn.commit();

        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Delete Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
        } finally {
            try {
                if (ps != null) {
                    ps.close();
                }
                if (set != null) {
                    set.close();
                }
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Delete Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
    }

    public static HashMap<String, Plot> getPlots(String world) {
        HashMap<String, Plot> ret = new HashMap<String, Plot>();
        Statement statementPlot = null;
        Statement statementAllowed = null;
        Statement statementDenied = null;
        Statement statementComment = null;
        ResultSet setPlots = null;
        ResultSet setAllowed = null;
        ResultSet setDenied = null;
        ResultSet setComments = null;

        try {
            Connection conn = getConnection();

            statementPlot = conn.createStatement();
            setPlots = statementPlot.executeQuery("SELECT * FROM plotmePlots WHERE LOWER(world) = '" + world + "'");
            int size = 0;
            while (setPlots.next()) {
                size++;
                int idX = setPlots.getInt("idX");
                int idZ = setPlots.getInt("idZ");
                String owner = setPlots.getString("owner");
                int topX = setPlots.getInt("topX");
                int bottomX = setPlots.getInt("bottomX");
                int topZ = setPlots.getInt("topZ");
                int bottomZ = setPlots.getInt("bottomZ");
                String biome = setPlots.getString("biome");
                java.sql.Date expireddate = setPlots.getDate("expireddate");
                boolean finished = setPlots.getBoolean("finished");
                PlayerList allowed = new PlayerList();
                PlayerList denied = new PlayerList();
                List<String[]> comments = new ArrayList<String[]>();
                double customprice = setPlots.getDouble("customprice");
                boolean forsale = setPlots.getBoolean("forsale");
                String finisheddate = setPlots.getString("finisheddate");
                boolean protect = setPlots.getBoolean("protected");
                String currentbidder = setPlots.getString("currentbidder");
                double currentbid = setPlots.getDouble("currentbid");
                boolean auctionned = setPlots.getBoolean("auctionned");

                byte[] byOwner = setPlots.getBytes("ownerId");
                byte[] byBidder = setPlots.getBytes("currentbidderid");

                UUID ownerId = null;
                UUID currentbidderid = null;

                if (byOwner != null) {
                    ownerId = UUIDFetcher.fromBytes(byOwner);
                }
                if (byBidder != null) {
                    currentbidderid = UUIDFetcher.fromBytes(byBidder);
                }

                statementAllowed = conn.createStatement();
                setAllowed = statementAllowed.executeQuery("SELECT * FROM plotmeAllowed WHERE idX = '" + idX + "' AND idZ = '" + idZ + "' AND LOWER(world) = '" + world + "'");

                while (setAllowed.next()) {
                    byte[] byPlayerId = setAllowed.getBytes("playerid");
                    if (byPlayerId == null) {
                        allowed.put(setAllowed.getString("player"));
                    } else {
                        allowed.put(setAllowed.getString("player"), UUIDFetcher.fromBytes(byPlayerId));
                    }
                }

                if (setAllowed != null)
                    setAllowed.close();

                statementDenied = conn.createStatement();
                setDenied = statementDenied.executeQuery("SELECT * FROM plotmeDenied WHERE idX = '" + idX + "' AND idZ = '" + idZ + "' AND LOWER(world) = '" + world + "'");

                while (setDenied.next()) {
                    byte[] byPlayerId = setAllowed.getBytes("playerid");
                    if (byPlayerId == null) {
                        denied.put(setDenied.getString("player"));
                    } else {
                        denied.put(setDenied.getString("player"), UUIDFetcher.fromBytes(byPlayerId));
                    }
                }

                if (setDenied != null)
                    setDenied.close();

                statementComment = conn.createStatement();
                setComments = statementComment.executeQuery("SELECT * FROM plotmeComments WHERE idX = '" + idX + "' AND idZ = '" + idZ + "' AND LOWER(world) = '" + world + "'");

                while (setComments.next()) {
                    String[] comment = new String[2];
                    comment[0] = setComments.getString("player");
                    comment[1] = setComments.getString("comment");
                    comment[2] = UUIDFetcher.fromBytes(setComments.getBytes("playerid")).toString();
                    comments.add(comment);
                }

                Plot plot = new Plot(owner, ownerId, world, topX, bottomX, topZ, bottomZ, biome, expireddate, finished, allowed, comments, "" + idX + ";" + idZ, customprice, forsale, finisheddate, protect, currentbidder, currentbidderid, currentbid, auctionned, denied);
                ret.put("" + idX + ";" + idZ, plot);
            }
            PlotMe.logger.info(PlotMe.NAME + " " + size + " plots loaded");
        } catch (SQLException ex) {
            PlotMe.logger.severe(PlotMe.NAME + " Load Exception :");
            PlotMe.logger.severe("  " + ex.getMessage());
            ex.printStackTrace();
        } finally {
            try {
                if (statementPlot != null)
                    statementPlot.close();
                if (statementAllowed != null)
                    statementAllowed.close();
                if (statementComment != null)
                    statementComment.close();
                if (setPlots != null)
                    setPlots.close();
                if (setComments != null)
                    setComments.close();
                if (setAllowed != null)
                    setAllowed.close();
            } catch (SQLException ex) {
                PlotMe.logger.severe(PlotMe.NAME + " Load Exception (on close) :");
                PlotMe.logger.severe("  " + ex.getMessage());
            }
        }
        return ret;
    }

    public static void plotConvertToUUIDAsynchronously() {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PlotMe.self, new Runnable() {
            @Override
            public void run() {
                PlotMe.logger.info("Checking if conversion to UUID needed...");

                boolean boConversion = false;
                Statement statementPlayers = null;
                PreparedStatement psOwnerId = null;
                PreparedStatement psCurrentBidderId = null;
                PreparedStatement psAllowedPlayerId = null;
                PreparedStatement psDeniedPlayerId = null;
                PreparedStatement psCommentsPlayerId = null;
                ResultSet setPlayers = null;
                int nbConverted = 0;
                String sql = "";
                int count = 0;

                try {
                    Connection conn = getConnection();

                    // Get all the players
                    statementPlayers = conn.createStatement();
                    // Exclude groups and names with * or missing
                    sql = "SELECT LOWER(owner) as Name FROM plotmePlots WHERE NOT owner IS NULL AND Not owner LIKE 'group:%' AND Not owner LIKE '%*%' AND ownerid IS NULL GROUP BY LOWER(owner) ";
                    sql = sql + "UNION SELECT LOWER(currentbidder) as Name FROM plotmePlots WHERE NOT currentbidder IS NULL AND currentbidderid IS NULL GROUP BY LOWER(currentbidder) ";
                    sql = sql + "UNION SELECT LOWER(player) as Name FROM plotmeAllowed WHERE NOT player IS NULL AND Not player LIKE 'group:%' AND Not player LIKE '%*%' AND playerid IS NULL GROUP BY LOWER(player) ";
                    sql = sql + "UNION SELECT LOWER(player) as Name FROM plotmeDenied WHERE NOT player IS NULL AND Not player LIKE 'group:%' AND Not player LIKE '%*%' AND playerid IS NULL GROUP BY LOWER(player) ";
                    sql = sql + "UNION SELECT LOWER(player) as Name FROM plotmeComments WHERE NOT player IS NULL AND Not player LIKE 'group:%' AND Not player LIKE '%*%' AND playerid IS NULL GROUP BY LOWER(player)";

                    PlotMe.logger.info("Verifying if database needs conversion");

                    setPlayers = statementPlayers.executeQuery(sql);

                    if (setPlayers.next()) {

                        List<String> names = new ArrayList<String>();

                        PlotMe.logger.info("Starting to convert plots to UUID");
                        do {
                            if (!setPlayers.getString("Name").equals("")) {
                                names.add(setPlayers.getString("Name"));
                            }
                        } while (setPlayers.next());

                        UUIDFetcher fetcher = new UUIDFetcher(names);

                        Map<String, UUID> response = null;

                        try {
                            PlotMe.logger.info("Fetching " + names.size() + " UUIDs from Mojang servers...");
                            response = fetcher.call();
                            PlotMe.logger.info("Finished fetching " + response.size() + " UUIDs. Starting database update.");
                        } catch (Exception e) {
                            PlotMe.logger.warning("Exception while running UUIDFetcher");
                            e.printStackTrace();
                        }

                        if (response.size() > 0) {
                            psOwnerId = conn.prepareStatement("UPDATE plotmePlots SET ownerid = ? WHERE LOWER(owner) = ? AND ownerid IS NULL");
                            psCurrentBidderId = conn.prepareStatement("UPDATE plotmePlots SET currentbidderid = ? WHERE LOWER(currentbidder) = ? AND currentbidderid IS NULL");
                            psAllowedPlayerId = conn.prepareStatement("UPDATE plotmeAllowed SET playerid = ? WHERE LOWER(player) = ? AND playerid IS NULL");
                            psDeniedPlayerId = conn.prepareStatement("UPDATE plotmeDenied SET playerid = ? WHERE LOWER(player) = ? AND playerid IS NULL");
                            psCommentsPlayerId = conn.prepareStatement("UPDATE plotmeComments SET playerid = ? WHERE LOWER(player) = ? AND playerid IS NULL");

                            for (String key : response.keySet()) {
                                count = 0;
                                // Owner
                                psOwnerId.setBytes(1, UUIDFetcher.toBytes(response.get(key)));
                                psOwnerId.setString(2, key.toLowerCase());
                                count += psOwnerId.executeUpdate();
                                // Bidder
                                psCurrentBidderId.setBytes(1, UUIDFetcher.toBytes(response.get(key)));
                                psCurrentBidderId.setString(2, key.toLowerCase());
                                count += psCurrentBidderId.executeUpdate();
                                // Allowed
                                psAllowedPlayerId.setBytes(1, UUIDFetcher.toBytes(response.get(key)));
                                psAllowedPlayerId.setString(2, key.toLowerCase());
                                count += psAllowedPlayerId.executeUpdate();
                                // Denied
                                psDeniedPlayerId.setBytes(1, UUIDFetcher.toBytes(response.get(key)));
                                psDeniedPlayerId.setString(2, key.toLowerCase());
                                count += psDeniedPlayerId.executeUpdate();
                                // Commenter
                                psCommentsPlayerId.setBytes(1, UUIDFetcher.toBytes(response.get(key)));
                                psCommentsPlayerId.setString(2, key.toLowerCase());
                                psCommentsPlayerId.executeUpdate();
                                conn.commit();
                                if (count > 0) {
                                    nbConverted++;
                                } else {
                                    PlotMe.logger.warning("Unable to update player '" + key + "'");
                                }
                            }

                            psOwnerId.close();
                            psCurrentBidderId.close();
                            psAllowedPlayerId.close();
                            psDeniedPlayerId.close();
                            psCommentsPlayerId.close();
                        }

                        boConversion = true;
                        PlotMe.logger.info(nbConverted + " players converted");
                    }
                    setPlayers.close();
                    statementPlayers.close();

                    if (boConversion) {
                        PlotMe.logger.info("Plot conversion finished");
                    } else {
                        PlotMe.logger.info("No plot conversion needed");
                    }
                } catch (SQLException ex) {
                    PlotMe.logger.severe("Conversion to UUID failed :");
                    PlotMe.logger.severe("  " + ex.getMessage());
                    for (StackTraceElement e : ex.getStackTrace()) {
                        PlotMe.logger.severe("  " + e.toString());
                    }
                } finally {
                    try {
                        if (statementPlayers != null) {
                            statementPlayers.close();
                        }
                        if (psOwnerId != null) {
                            psOwnerId.close();
                        }
                        if (psCurrentBidderId != null) {
                            psCurrentBidderId.close();
                        }
                        if (psAllowedPlayerId != null) {
                            psAllowedPlayerId.close();
                        }
                        if (psDeniedPlayerId != null) {
                            psDeniedPlayerId.close();
                        }
                        if (psCommentsPlayerId != null) {
                            psCommentsPlayerId.close();
                        }
                        if (setPlayers != null) {
                            setPlayers.close();
                        }
                    } catch (SQLException ex) {
                        PlotMe.logger.severe("Conversion to UUID failed (on close) :");
                        PlotMe.logger.severe("  " + ex.getMessage());
                        for (StackTraceElement e : ex.getStackTrace()) {
                            PlotMe.logger.severe("  " + e.toString());
                        }
                    }
                }
            }
        });
    }

    public static void fetchOwnerUUIDAsync(int idX, int idZ, String world, String owner) {
        _fetchUUIDAsync(idX, idZ, world, "owner", owner);
    }

    public static void fetchBidderUUIDAsync(int idX, int idZ, String world, String bidder) {
        _fetchUUIDAsync(idX, idZ, world, "bidder", bidder);
    }

    public static void fetchCommenterUUIDAsync(int idX, int idZ, String world, int commentid, String commenter) {
        _fetchUUIDAsync(idX, idZ, world, "commenter", commenter);
    }

    public static void fetchAllowedUUIDAsync(int idX, int idZ, String world, String allowed) {
        _fetchUUIDAsync(idX, idZ, world, "allowed", allowed);
    }

    public static void fetchDeniedUUIDAsync(int idX, int idZ, String world, String denied) {
        _fetchUUIDAsync(idX, idZ, world, "denied", denied);
    }

    private static void _fetchUUIDAsync(final int idX, final int idZ, final String world, final String Property, final String name) {
        Bukkit.getServer().getScheduler().runTaskAsynchronously(PlotMe.self, new Runnable() {
            @Override
            public void run() {

                PreparedStatement ps = null;

                try {
                    Connection conn = getConnection();

                    List<String> names = new ArrayList<String>();

                    names.add(name);

                    UUIDFetcher fetcher = new UUIDFetcher(names);

                    Map<String, UUID> response = null;

                    try {
                        PlotMe.logger.info("Fetching " + names.size() + " UUIDs from Mojang servers...");
                        response = fetcher.call();
                        PlotMe.logger.info("Finished fetching " + response.size() + " UUIDs. Starting database update.");
                    } catch (Exception e) {
                        PlotMe.logger.warning("Exception while running UUIDFetcher");
                        e.printStackTrace();
                    }

                    if (response.size() > 0) {
                        switch (Property) {
                        case "owner":
                            ps = conn.prepareStatement("UPDATE plotmePlots SET ownerid = ? WHERE LOWER(owner) = ? AND idX = '" + idX + "' AND idZ = '" + idZ + "' AND LOWER(world) = '" + world + "'");
                            break;
                        case "bidder":
                            ps = conn.prepareStatement("UPDATE plotmePlots SET currentbidderid = ? WHERE LOWER(currentbidder) = ? AND idX = '" + idX + "' AND idZ = '" + idZ + "' AND LOWER(world) = '" + world + "'");
                            break;
                        case "allowed":
                            ps = conn.prepareStatement("UPDATE plotmeAllowed SET playerid = ? WHERE LOWER(player) = ? AND idX = '" + idX + "' AND idZ = '" + idZ + "' AND LOWER(world) = '" + world + "'");
                            break;
                        case "denied":
                            ps = conn.prepareStatement("UPDATE plotmeDenied SET playerid = ? WHERE LOWER(player) = ? AND idX = '" + idX + "' AND idZ = '" + idZ + "' AND LOWER(world) = '" + world + "'");
                            break;
                        case "commenter":
                            ps = conn.prepareStatement("UPDATE plotmeComments SET playerid = ? WHERE LOWER(player) = ? AND idX = '" + idX + "' AND idZ = '" + idZ + "' AND LOWER(world) = '" + world + "'");
                            break;
                        default:
                            return;
                        }

                        for (String key : response.keySet()) {
                            ps.setBytes(1, UUIDFetcher.toBytes(response.get(key)));
                            ps.setString(2, key.toLowerCase());
                            ps.executeUpdate();
                            conn.commit();
                        }

                        ps.close();
                    }

                } catch (SQLException ex) {
                    PlotMe.logger.severe("Conversion to UUID failed :");
                    PlotMe.logger.severe("  " + ex.getMessage());
                    for (StackTraceElement e : ex.getStackTrace()) {
                        PlotMe.logger.severe("  " + e.toString());
                    }
                } finally {
                    try {
                        if (ps != null) {
                            ps.close();
                        }
                    } catch (SQLException ex) {
                        PlotMe.logger.severe("Conversion to UUID failed (on close) :");
                        PlotMe.logger.severe("  " + ex.getMessage());
                        for (StackTraceElement e : ex.getStackTrace()) {
                            PlotMe.logger.severe("  " + e.toString());
                        }
                    }
                }
            }
        });
    }

    /*private static UUID fromBlobToUUID(Blob blob) {
        try {
            int size = (int) blob.length();
            return UUIDFetcher.fromBytes(blob.getBytes(1, size));
        } catch (SQLException e) {
            return null;
        }
    }*/

    private static Blob fromUUIDToBlob(UUID uuid) {
        Blob blob = null;
        try {
            blob = new javax.sql.rowset.serial.SerialBlob(UUIDFetcher.toBytes(uuid));
        } catch (SQLException e) {
            return null;
        }
        return blob;
    }
}
