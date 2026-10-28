package academy.backend.market_pulse.history.impl;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import academy.backend.market_pulse.history.PriceHistoryStore;
import academy.backend.market_pulse.history.PricePoint;

/**
 * Заготовка для самостоятельной работы (см. «Требования к файловому хранилищу.md», семинар 7) —
 * структура есть, решения нет. Формат файла — на ваш выбор (бинарный, текстовый и т.д.); главное,
 * чтобы каждый {@link #append} создавал НОВЫЙ файл (не переписывал предыдущий) и
 * {@link #load(Path, String)} возвращал все сохранённые точки по тикеру в порядке возрастания
 * времени. Проверьте себя тестом {@code PriceHistoryStoreContractTest} (замените в нём эталонную
 * реализацию на свою).
 */
public class PriceHistoryStoreSkeleton implements PriceHistoryStore {

    private static final String SEPARATOR = ";";
    private static final String EXTENSION = ".csv";

    @Override
    public Path append(Path directory, PricePoint point) {
        // TODO: записать точку в новый файл внутри directory — существующие файлы не трогать.
        //  Подсказка: имя файла на основе point.timestamp() даёт сортировку по имени = сортировку
        //  по времени.
        String line = point.ticker() + SEPARATOR + point.timestamp() + SEPARATOR
                + point.price().toPlainString();
        // Время дополнено нулями до фиксированной длины, чтобы сортировка по имени совпадала с
        // сортировкой по времени: SBER_00000000001700000000_000000000.csv
        String baseName = point.ticker() + "_"
                + String.format("%020d_%09d", point.timestamp().getEpochSecond(), point.timestamp().getNano());
        try {
            Files.createDirectories(directory);
            // CREATE_NEW не даёт переписать существующий файл: если точка с тем же временем уже есть,
            // берём следующее свободное имя с суффиксом.
            for (int attempt = 0; ; attempt++) {
                String suffix = attempt == 0 ? "" : "_" + attempt;
                Path file = directory.resolve(baseName + suffix + EXTENSION);
                try {
                    Files.writeString(file, line, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE);
                    return file;
                } catch (FileAlreadyExistsException e) {
                    // имя занято - пробуем следующее
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось записать точку истории цены в " + directory, e);
        }
    }

    @Override
    public List<PricePoint> load(Path directory, String ticker) {
        // TODO: прочитать все точки по тикеру в порядке возрастания времени.
        //  Если для тикера ещё нет точек — вернуть пустой список, а не бросать исключение.
        if (!Files.isDirectory(directory)) {
            return List.of();
        }
        String prefix = ticker + "_";
        try (Stream<Path> files = Files.list(directory)) {
            return files
                    .filter(file -> {
                        String name = file.getFileName().toString();
                        return name.startsWith(prefix) && name.endsWith(EXTENSION);
                    })
                    .map(PriceHistoryStoreSkeleton::read)
                    // по префиксу мог попасть другой тикер (например, «SBER» и «SBER_P») - сверяем точно
                    .filter(point -> point.ticker().equals(ticker))
                    .sorted(Comparator.comparing(PricePoint::timestamp))
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать историю цены из " + directory, e);
        }
    }

    private static PricePoint read(Path file) {
        try {
            String[] parts = Files.readString(file).strip().split(SEPARATOR);
            return new PricePoint(parts[0], Instant.parse(parts[1]), new BigDecimal(parts[2]));
        } catch (IOException e) {
            throw new UncheckedIOException("Не удалось прочитать точку истории цены из " + file, e);
        }
    }
}
