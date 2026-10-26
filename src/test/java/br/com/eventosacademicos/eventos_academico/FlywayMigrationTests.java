package br.com.eventosacademicos.eventos_academico;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import java.sql.DriverManager;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FlywayMigrationTests {

    @Test
    void deveAtualizarBancoAteV4EPreservarDadosAoExecutarNovamente() throws Exception {
        String url = "jdbc:h2:mem:migration-" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1";
        Flyway versaoInicial = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .target("1")
                .load();

        assertThat(versaoInicial.migrate().migrationsExecuted).isEqualTo(1);

        UUID id = UUID.randomUUID();
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.prepareStatement(
                     "INSERT INTO usuario (id, nome, email, senha) VALUES (?, ?, ?, ?)"
             )) {
            statement.setObject(1, id);
            statement.setString(2, "Maria");
            statement.setString(3, "maria@example.com");
            statement.setString(4, "hash-de-teste");
            assertThat(statement.executeUpdate()).isEqualTo(1);
        }

        Flyway versaoCategoria = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .target("2")
                .load();

        assertThat(versaoCategoria.migrate().migrationsExecuted).isEqualTo(1);

        UUID categoriaId = UUID.randomUUID();
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.prepareStatement("INSERT INTO categoria (id, nome) VALUES (?, ?)")) {
            statement.setObject(1, categoriaId);
            statement.setString(2, "Palestra");
            assertThat(statement.executeUpdate()).isEqualTo(1);
        }

        Flyway versaoEvento = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .target("3")
                .load();

        assertThat(versaoEvento.migrate().migrationsExecuted).isEqualTo(1);

        UUID eventoId = UUID.randomUUID();
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.prepareStatement(
                     "INSERT INTO evento (id, titulo, descricao, inicio, fim, capacidade, categoria_id) VALUES (?, ?, ?, ?, ?, ?, ?)"
             )) {
            statement.setObject(1, eventoId);
            statement.setString(2, "Semana acadêmica");
            statement.setString(3, "Encontro dos estudantes");
            statement.setObject(4, java.time.LocalDateTime.of(2030, 11, 10, 9, 0));
            statement.setObject(5, java.time.LocalDateTime.of(2030, 11, 10, 12, 0));
            statement.setInt(6, 50);
            statement.setObject(7, categoriaId);
            assertThat(statement.executeUpdate()).isEqualTo(1);
        }

        Flyway flyway = Flyway.configure()
                .dataSource(url, "sa", "")
                .locations("classpath:db/migration")
                .load();

        assertThat(flyway.migrate().migrationsExecuted).isEqualTo(1);

        UUID inscricaoId = UUID.randomUUID();
        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.prepareStatement(
                     "INSERT INTO inscricao (id, usuario_id, evento_id, data_inscricao) VALUES (?, ?, ?, ?)"
             )) {
            statement.setObject(1, inscricaoId);
            statement.setObject(2, id);
            statement.setObject(3, eventoId);
            statement.setObject(4, java.time.LocalDateTime.of(2030, 11, 1, 10, 0));
            assertThat(statement.executeUpdate()).isEqualTo(1);

            statement.setObject(1, UUID.randomUUID());
            assertThatThrownBy(() -> statement.executeUpdate())
                    .isInstanceOf(java.sql.SQLException.class)
                    .extracting("SQLState").isEqualTo("23505");
        }

        assertThat(flyway.migrate().migrationsExecuted).isZero();
        assertThat(flyway.info().applied()).hasSize(4);
        assertThat(flyway.info().current().getVersion().toString()).isEqualTo("4");

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.prepareStatement("SELECT email FROM usuario WHERE id = ?")) {
            statement.setObject(1, id);
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("email")).isEqualTo("maria@example.com");
                assertThat(result.next()).isFalse();
            }
        }

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.prepareStatement("SELECT nome FROM categoria WHERE id = ?")) {
            statement.setObject(1, categoriaId);
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("nome")).isEqualTo("Palestra");
                assertThat(result.next()).isFalse();
            }
        }

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.prepareStatement(
                     "SELECT e.titulo, c.nome FROM evento e JOIN categoria c ON c.id = e.categoria_id WHERE e.id = ?"
             )) {
            statement.setObject(1, eventoId);
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("titulo")).isEqualTo("Semana acadêmica");
                assertThat(result.getString("nome")).isEqualTo("Palestra");
                assertThat(result.next()).isFalse();
            }
        }

        try (var connection = DriverManager.getConnection(url, "sa", "");
             var statement = connection.prepareStatement(
                     "SELECT u.email, e.titulo FROM inscricao i "
                             + "JOIN usuario u ON u.id = i.usuario_id "
                             + "JOIN evento e ON e.id = i.evento_id WHERE i.id = ?"
             )) {
            statement.setObject(1, inscricaoId);
            try (var result = statement.executeQuery()) {
                assertThat(result.next()).isTrue();
                assertThat(result.getString("email")).isEqualTo("maria@example.com");
                assertThat(result.getString("titulo")).isEqualTo("Semana acadêmica");
                assertThat(result.next()).isFalse();
            }
        }
    }
}
