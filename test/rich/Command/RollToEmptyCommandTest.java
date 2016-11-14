package rich.Command;

import org.junit.Before;
import org.junit.Test;
import rich.Dice;
import rich.Map;
import rich.Place.Estate;
import rich.Place.Place;
import rich.Player;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RollToEmptyCommandTest {

    private Map map;
    private Dice dice;
    private Place empty;
    private Place starting;

    @Before
    public void setUp() throws Exception {
        map = mock(Map.class);
        dice = mock(Dice.class);
        when(dice.next()).thenReturn(1);
        empty = new Estate(200);
        starting = mock(Place.class);
        when(map.move(eq(starting), eq(1))).thenReturn(empty);
    }

    @Test
    public void should_be_wait_response_after_roll_to_empty() throws Exception {
        Player player = Player.createPlayerWithStarting(starting);

        Command rollCommand = new RollCommand(map, dice);

        player.executeCommand(rollCommand);

        assertThat(player.getStatus(), is(Player.Status.WAIT_RESPONSE));
    }


}