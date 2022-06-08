package nl.tijsbeek.database.databases;

import com.diffplug.common.base.Errors;
import nl.tijsbeek.database.tables.UserSocial;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class UserSocialDatabase extends AbstractDatabase<List<UserSocial>> implements IStringDatabase<List<UserSocial>> {
    private static final Logger logger = LoggerFactory.getLogger(UserSocialDatabase.class);

    @Contract(pure = true)
    UserSocialDatabase(@NotNull final Database database) {
        super(database.getDataSource());
    }

    @Override
    public List<UserSocial> retrieveById(long id) {
        return withReturn("""
                SELECT *
                FROM discordbot.user_socials
                WHERE id = ?
                """, setIdLongConsumer(id), Errors.rethrow().wrap(UserSocialDatabase::resultSetToUserSocials));
    }

    @Override
    public List<UserSocial> deleteById(long id) {
        return withReturn("""
                DELETE FROM discordbot.user_socials
                WHERE id = ?
                RETURNING *
                """, setIdLongConsumer(id), Errors.rethrow().wrap(UserSocialDatabase::resultSetToUserSocials));
    }

    @Override
    public void insert(@NotNull List<UserSocial> paramUserSocials) {
        List<UserSocial> userSocials = paramUserSocials.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();


        if (userSocials.isEmpty()) {
            return;
        }

        @Language("SQL")
        String string = """
                INSERT INTO discordbot.user_socials (id, platform_name, platform_icon, platform_user_url, platform_user_name)
                VALUES
                      (?, ?, ?, ?, ?)
                """ +
                ",\n      (?, ?, ?, ?, ?)".repeat(userSocials.size()) +
                ';';


        withoutReturn(string, setUserSocial(userSocials));
    }

    @Override
    public void replace(@NotNull List<UserSocial> userSocials) {
    }

    @NotNull
    private static List<UserSocial> resultSetToUserSocials(final ResultSet resultSet) throws SQLException {
        List<UserSocial> socials = new ArrayList<>(10);

        while (null != resultSet && resultSet.next()) {
            long id = resultSet.getLong("id");
            String platformName = resultSet.getString("platform_name");
            String platformIcon = resultSet.getString("platform_icon");
            String platformUserUrl = resultSet.getString("platform_user_url");
            String platformUserName = resultSet.getString("platform_user_name");

            socials.add(new UserSocial(id)
                    .setPlatform(platformName, platformIcon)
                    .setPlatformUser(platformUserName, platformUserUrl));
        }

        return socials;
    }

    private Consumer<? super PreparedStatement> setUserSocial(List<UserSocial> userSocials) {
        return Errors.rethrow().wrap(statement -> {
            statement.setString(1, userSocials.getI);
            statement.setObject(2, componentEntity.getExpireDate());
            statement.setString(3, Database.argumentsToCsvString(componentEntity.getArguments()));
        });
    }

}
