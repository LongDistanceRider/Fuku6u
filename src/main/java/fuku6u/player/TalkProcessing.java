package fuku6u.player;

import fuku6u.board.BoardSurface;
import fuku6u.log.Log;
import fuku6u.observation.Observation;
import fuku6u.possessedExpectation.PossessedExpectation;
import fuku6u.wolfGroupExpectation.WolfGroupExpectation;
import org.aiwolf.client.lib.Content;
import org.aiwolf.common.data.Talk;

import java.util.List;

/**
 * 発言処理を担うクラス
 * 主にupdate()で呼び出される
 */
public class TalkProcessing {
    /* トークリストをどこまで読み込んだか */
    private static int talkListHead = 0;

    // 必要な要素を必要な形でBoardSurfaceに書き込む AGREE DISAGREEのためにTalkを全て保管
    public static void update(List<Talk> talkList, BoardSurface boardSurface, WolfGroupExpectation wExpect, PossessedExpectation pExpect) {
        for (int i = talkListHead; i < talkList.size(); i++) {
            Talk talk = talkList.get(i);
            Log.info("Taker: " + talk.getAgent() + " mes: " + talk.getText());
            if (talk.getAgent().equals(boardSurface.getMe())) {  // 自分自身の発言はスキップ
                continue;
            }
            // Talkを保管
            boardSurface.addTalk(talk);
            // String text を Contentに変換する
            Content content = new Content(talk.getText());
            // ラベルごとに処理
            switch (content.getTopic()) {
            /* --- 意図表明に関する文 --- */
                case COMINGOUT:
                    Log.debug("Taker: " + talk.getAgent() + " CO: " + content.getRole());
                    boardSurface.addComingoutRole(talk.getAgent(), content.getRole()); // CO役職を保管
                    Observation.comingout(boardSurface, wExpect, pExpect, talk.getAgent(), content.getRole());
                    break;
                case ESTIMATE:
                    break;
            /* --- 能力結果に関する文 --- */
                case DIVINED:
                    Log.debug("Taker: " + talk.getAgent() + " Target: " + content.getTarget() + " DIV: " + content.getResult());
                    boardSurface.addDivMap(talk.getAgent(), content.getTarget(), content.getResult()); // 占い結果を保管
                    Observation.divined(boardSurface, wExpect, pExpect, talk.getAgent(), content.getTarget(), content.getResult());
                    break;
                case IDENTIFIED:
                    Log.debug("Taker: " + talk.getAgent() + " Target: " + content.getTarget() + " DIV: " + content.getResult());
                    boardSurface.addIdenMap(talk.getAgent(), content.getTarget(), content.getResult()); // 霊能結果を保管
                    break;
//                case GUARDED:
//                    break;
//            /* --- ルール行動・能力に関する文 --- */
//                case DIVINATION:
//                    break;
//                case GUARD:
//                    break;
                case VOTE:
                    Log.debug("Taker: " + talk.getAgent() + " Target: " + content.getTarget());
                    boardSurface.addVote(talk.getAgent(), content.getTarget()); // 投票先発言を保管
                    break;
//                case ATTACK:
//                    break;
//            /* --- 同意・非同意に関する文 --- */
//                case AGREE:
//                    break;
//                case DISAGREE:
//                    break;
//            /* --- 発話制御に関する文 --- */
//                case OVER:
//                    break;
//                case SKIP:
//                    break;
//            /* --- REQUEST文 --- */
//                case OPERATOR:
//                    break;
                default:
                    break;
            }
//            Log.info(">> " + talk.getAgent() + " : " + talk.getText());
        }
        talkListHead = talkList.size();
    }
}
