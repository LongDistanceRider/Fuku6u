import org.aiwolf.common.data.Role;

public class Main {

    private final static String HOST = "localhost"; // ホスト名 (kanolab 49.212.130.102)
    private final static int PORT = 10000;  // ポート番号
    private final static int NUMBER_OF_GAME = 2;  // 試合回数
    private final static int NUMBER_OF_PLAYER = 5;  // 対戦人数

    public static void main(String[] args) throws InterruptedException {
        Starter starter = new Starter(HOST, PORT, NUMBER_OF_GAME, NUMBER_OF_PLAYER);  // ゲームスタートのインスタンス作成
        starter.serverStart();  // ローカルサーバの立ち上げ
        Thread.sleep(1000); // ここで1秒待たないと，サーバ立ち上がる前にクライアント接続してしまう
        starter.connectClient("fuku6u.player.Fuku6u", "Fuku6u", Role.VILLAGER);
        starter.connectClient("net.mchs_u.mc.aiwolf.curry.McrePlayer", "Mcre", null);
        starter.connectClient("fuku6u.player.Dammy", "Dammy", null);
        starter.connectClient("fuku6u.player.Dammy", "Dammy", null);
        starter.connectClient("fuku6u.player.Dammy", "Dammy", null);
    }

}
