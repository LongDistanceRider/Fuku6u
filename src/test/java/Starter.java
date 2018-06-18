import fuku6u.log.Log;
import org.aiwolf.common.data.Player;
import org.aiwolf.common.data.Role;
import org.aiwolf.common.net.GameSetting;
import org.aiwolf.common.net.TcpipClient;
import org.aiwolf.server.AIWolfGame;
import org.aiwolf.server.net.TcpipServer;
import org.aiwolf.server.util.FileGameLogger;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * ゲームサーバの立ち上げ，ゲーム開始に必要な処理をまとめたクラス
 */
class Starter {

    private String host = "";
    private int port = 0;
    private int numberOfGame = 0;
    private int numberOfPlayer = 0;
    private List<Player> participantPlayerList = new ArrayList<>();
    private Queue<Role> desiredRole = new ArrayDeque<>();

    /**
     * コンストラクター
     * @param host 接続先ホストめい
     * @param port 接続先ポート番号
     * @param numberOfGame　ゲーム回数
     */
    Starter(String host, int port, int numberOfGame, int numberOfPlayer) {
        this.host = host;
        this.port = port;
        this.numberOfGame = numberOfGame;
        this.numberOfPlayer = numberOfPlayer;
    }

    /**
     * 参加者プレイヤーの追加
     * @param player　参加プレイヤ
     */
    void setClient(Player player, Role desiredRole) {
        participantPlayerList.add(player);
        this.desiredRole.add(desiredRole);
    }

    /**
     * ローカルサーバの立ち上げ
     */
    void serverStart() {
        new Thread(() -> {
            try {
                GameSetting gameSetting = GameSetting.getCustomGame(System.getProperty("user.dir") + "/lib/Setting.cfg",numberOfPlayer);
                TcpipServer gameServer = new TcpipServer(port, numberOfPlayer, gameSetting);
                gameServer.waitForConnection();
                AIWolfGame game = new AIWolfGame(gameSetting, gameServer);
                game.setShowConsoleLog(false);   // サーバのコンソールログ出力

                for (int i = 0; i < numberOfGame; i++) {
                    game.setRand(new Random(i));
                    Calendar calendar = Calendar.getInstance();
                    game.setGameLogger(new FileGameLogger(new File("log/" + calendar.getTime() + ".log")));

                    game.start();
                }
            } catch (IOException e) {
                System.err.println("ローカルサーバ立ち上げでIOException発生．ログの出力に失敗，サーバ設定ファイル読み込み失敗した可能性があります．");
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * ゲーム開始
     */
    @Deprecated
    void gameStart() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for (Player player :
                participantPlayerList) {
            TcpipClient client;

            if (desiredRole.isEmpty()) {
                client = new TcpipClient(host, port);
            } else {
                client = new TcpipClient(host, port, desiredRole.poll());
            }


            client.connect(player);
            client.setName(player.getName());
            Log.info(player.getName() + " is connected");
        }
    }

    /**
     * クライアント接続します
     * 正しいパラメータを設定しない場合は例外をスローします．
     *
     * 役職の希望提出が可能
     * @param classPass
     * @param playerName
     * @param host
     * @param port
     * @param numConnectiuons
     * @param role
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    @Deprecated
    public static void startClient(String classPass, String playerName, String host, int port, int numConnectiuons, org.aiwolf.common.data.Role role) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        for (int i = 0; i < numConnectiuons; i++) {
            TcpipClient client = new TcpipClient(host, port, role);
            Class<?> class1 = Class.forName(classPass);
            Player player = (Player) class1.newInstance();
            client.connect(player);
            client.setName(playerName);
            System.out.println(playerName + "が接続されました．");
        }
    }

    /**
     * クライアント接続
     * @param classPass
     * @param playerName
     * @param role
     */
    void connectClient(String classPass, String playerName, Role role) {
        TcpipClient client = new TcpipClient(host, port, role);
        Class<?> class_;
        Player player = null;
        try {
            class_ = Class.forName(classPass);
            player = (Player) class_.newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        client.connect(player);
        client.setName(playerName);
        System.out.println(playerName + " is connected");
    }
}