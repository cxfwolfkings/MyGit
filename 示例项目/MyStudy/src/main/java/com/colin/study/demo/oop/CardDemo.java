package demo.oop;
import java.util.Arrays;
import java.util.Random;
public class CardDemo {
	public static void main(String[] args){
		Card[] cards = new Card[54];
		int i=0;
		for(int rank=Card.THREE;rank<=Card.DEUCE;rank++){
			cards[i++] = new Card(Card.DIAMOND,rank);
			cards[i++] = new Card(Card.CLUB,rank);
			cards[i++] = new Card(Card.HEART,rank);
			cards[i++] = new Card(Card.SPADE,rank);
		}
		cards[i++] = new Card(Card.JOKER,Card.BLACK);
		cards[i++] = new Card(Card.JOKER,Card.COLOR);
		System.out.println(Arrays.toString(cards));
	    //ϴ��(�����㷨) 
	    // �㷨����: 1 ���һ������ǰ��ĳ���ƽ���, ϴ��һ����
	    //          2 ������2������ǰ��ĳ���ƽ���, ϴ��2����
	    //          3 ...
	    // i �������һ�ŵ�λ��, j����ǰ��ĳ�ŵ�λ��
	    /*Random random = new Random();
	      for(i=cards.length-1; i>0; i--){
	      int j = random.nextInt(i);
	      Card t = cards[i];
	      cards[i] = cards[j];
	      cards[j] = t; */
		cards = washCards(cards);
		System.out.println(Arrays.toString(cards));
		Player[] players = new Player[3];
		players[0] = new Player(1,"��������");
		players[1] = new Player(2,"����D·��");
		players[2] = new Player(3,"����һ��");
		int index = 0;
		for(int n=0;n<cards.length;n++){
			players[index++%3].add(cards[n]);
		}
		System.out.println(players[0]);
		System.out.println(players[1]);
		System.out.println(players[2]);
	}
	public static Card[] washCards(Card[] cards){
		Random random = new Random();
		for(int i=0;i<cards.length;i++){
			int n = random.nextInt(cards.length-i)+i;
			Card card = cards[i];
			cards[i] = cards[n];
			cards[n] = card;
		}
		return cards;
	}
}
