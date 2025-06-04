package Players;

import cards.Card;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * 人类玩家
 * 实现了人类玩家的选牌等
 * */
public class HumanPlayer extends Player {

    public HumanPlayer(String name) {
        super(name);
    }

    @Override
    public List<Card> play(List<Card> lastCards) {
        Scanner scanner = new Scanner(System.in);

        List<Card> hand = getHand();
        for (int i = 0; i < hand.size(); i++) {
            System.out.println((i + 1) + ". " + hand.get(i).getDisplayName());
        }

        if (lastCards != null && !lastCards.isEmpty()) {
            System.out.println("上一手牌：");
            for (Card card : lastCards) {
                System.out.print(card.getDisplayName() + " ");
            }
            System.out.println();
        }

        System.out.println("请输入要出的牌的索引，多张牌用空格分隔(例如：1 3 5)，输入'P'过牌：");
        String input = scanner.nextLine().trim();

        if (input.equalsIgnoreCase("P")) {
            System.out.println(getName() + "选择不出牌");
            return Collections.emptyList();
        }

        List<Integer> cardIndices = new ArrayList<>();
        try {
            String[] indexStrings = input.split("\\s+");
            for (String indexStr : indexStrings) {
                int index = Integer.parseInt(indexStr) - 1;
                if (index < 0 || index >= hand.size()) {
                    System.out.println("无效的卡牌索引: " + (index + 1));
                    return play(lastCards);
                }
                cardIndices.add(index);
            }
        } catch (NumberFormatException e) {
            System.out.println("输入格式错误，请输入数字或'P'");
            return play(lastCards);
        }

        if (cardIndices.isEmpty()) {
            System.out.println("未选择任何牌，请重新选择");
            return play(lastCards);
        }

        System.out.print("你选择了: ");
        List<Card> selectedCards = new ArrayList<>();
        for (int index : cardIndices) {
            Card card = hand.get(index);
            selectedCards.add(card);
            System.out.print(card.getDisplayName() + " ");
        }
        System.out.println();

        return playCards(cardIndices);
    }
}