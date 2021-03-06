package rich.commander;

import org.fusesource.jansi.Ansi;
import rich.Printer;
import rich.Status;
import rich.command.Command;
import rich.map.Map;
import rich.place.Estate;
import rich.place.Hospital;
import rich.place.Place;
import rich.place.Prison;
import rich.tool.Tool;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;

public class Player implements Commander {
    private int id;
    private String name;
    private Status status;
    private Place currentPlace;
    private double balance;
    private List<Place> estates;
    private int freeTurns;
    private int waitTurn;
    private int points;
    private List<Tool> tools;

    public Player (int id, double balance){
        this.id = id;
        switch (id){
            case 1:
                name = "Q";
                break;
            case 2:
                name = "A";
                break;
            case 3:
                name = "S";
                break;
            case 4:
                name = "J";
                break;
        }
        this.balance = balance;
        status = Status.END_TURN;
        estates = new ArrayList<>();
        freeTurns = -1;
        waitTurn = -1;
        points = 0;
        tools = new ArrayList<>();
    }

    public Player (int id, double balance, Place startPoint){
        this.id = id;
        this.balance = balance;
        currentPlace = startPoint;
        status = Status.END_TURN;
        estates = new ArrayList<>();
        freeTurns = -1;
        waitTurn = -1;
        points = 0;
        tools = new ArrayList<>();
    }

    public static Player createPlayerWithStarting(Place starting) {
        Player player = new Player(1, 0);
        player.currentPlace = starting;
        return player;
    }

    public static Player createPlayerWithBalanceAndEstate(Place starting, double balance, Place... estates) {
        Player player = createPlayerWithStarting(starting);
        player.balance = balance;
        player.estates.addAll(asList(estates));
        return player;
    }

    public static Player createPlayerWithFreeTimes(Place starting, double balance, int freeTurns, Place... estates) {
        Player player = createPlayerWithBalanceAndEstate(starting, balance, estates);
        player.freeTurns = freeTurns;
        return player;
    }

    public static Player createPlayerWithPoints(Place starting, int points) {
        Player player = new Player(1, 0);
        player.currentPlace = starting;
        player.points = points;
        return player;
    }

    public void startTurn(){
        if(waitTurn == -1 && status == Status.END_TURN){
            status = Status.WAIT_COMMAND;
        }
        if(freeTurns >= 0)
            freeTurns --;
        if(waitTurn >= 0)
            waitTurn --;
    }

    @Override
    public void execute(Command response) {
        status = response.execute(this);
    }

    public void moveTo(Place place) {
        currentPlace = place;
    }

    public void buy() {
        Estate estate = (Estate) currentPlace;
        if (balance >= estate.getPrice() && estate.buy(this)) {
            balance -= ((Estate) currentPlace).getPrice();
            estates.add(currentPlace);
            return;
        }
    }

    public void promoteEstate() {
        Estate estate = ((Estate) currentPlace);
        if (balance >= estate.getPrice() && estate.promote()) {
            balance -= estate.getPrice();
        }
    }

    public boolean payFee() {
        Estate estate = (Estate) currentPlace;
        if (freeTurns >= 0 || estate.getOwner().getCurrentPlace() instanceof Hospital || estate.getOwner().getCurrentPlace() instanceof Prison) {
            return true;
        }
        double fee = estate.getPrice() * estate.getLevel().getFeeTimes();
        if (balance >= fee) {
            balance -= fee;
            estate.getOwner().gainFee(fee);
            return true;
        }else {
            estate.revert();
            return false;
        }
    }

    public void gainFee(Double fee) {
        balance += fee;
    }

    public void inPrison(int waitTurn) {
        this.waitTurn = waitTurn;
    }

    public void buyTool(int choice) {
        if (tools.size() < 10 && points >= Tool.Type.values()[choice - 1].getPointPrice()) {
            tools.add(new Tool(Tool.Type.values()[choice - 1]));
            points -= Tool.Type.values()[choice - 1].getPointPrice();
        }
    }

    public void selectGift(int choice){
        switch (choice){
            case 1:
                balance += 2000;
                break;
            case 2:
                points += 200;
                break;
            case 3:
                freeTurns = 5;
                break;
            default:
                break;
        }
    }

    public void goToHospital(Hospital hospital){
        currentPlace = hospital;
        waitTurn = 3;
    }

    public void gainPoints(int points){
        this.points += points;
    }

    public void sellEstate(Place target){
        if (target instanceof Estate){
            Estate estate = (Estate)target;
            if(estate.getOwner() == this){
                double income = estate.sell();
                balance += income;
                estates.remove(estate);
            }
        }
    }

    public void sellTool(Tool.Type type){
        Tool tool = tools
                .stream()
                .filter(t -> t.getType().equals(type))
                .findFirst()
                .orElse(null);
        if( tool != null){
            tools.remove(tool);
            points += tool.getPointPrice();
        }
    }

    public void useTool(Map map, int distance, Tool.Type type){
        for (Tool tool : tools){
            if(tool.getType() == type){
                if(map.useTool(currentPlace, distance, type)) {
                    tools.remove(tool);
                    break;
                }
            }
        }
    }

    public void query(){
        Printer.printAsset(this);
    }

    public Status getStatus() {
        return status;
    }

    public Place getCurrentPlace() {
        return currentPlace;
    }

    public double getBalance() {
        return balance;
    }

    public List<Place> getEstates() {
        return estates;
    }

    public int getWaitTurn() {
        return waitTurn;
    }

    public int getPoints() {
        return points;
    }

    public List<Tool> getTools() {
        return tools;
    }

    public int getFreeTurns() {
        return freeTurns;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Ansi getAnsi() {
        Ansi ansi;
        switch (id){
            case 1:
                name = "Q";
                ansi = ansi().eraseScreen().fg(RED);
                break;
            case 2:
                name = "A";
                ansi = ansi().eraseScreen().fg(BLUE);
                break;
            case 3:
                name = "S";
                ansi = ansi().eraseScreen().fg(GREEN);
                break;
            case 4:
                name = "J";
                ansi = ansi().eraseScreen().fg(MAGENTA);
                break;
            default:
                ansi = ansi().eraseScreen().fg(BLACK);
        }
        return ansi;
    }
}
