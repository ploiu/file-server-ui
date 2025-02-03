package ploiu.search;

import lombok.Builder;
import lombok.Data;

import java.util.regex.Pattern;

@Data
@Builder
public final class Attribute {
    static final Pattern BYTE_MULT_PATTERN = Pattern.compile("^(?<number>[0-9]*)(?<mult>ki?b|mi?b|gi?b|ti?b|pi?b)$", Pattern.CASE_INSENSITIVE);
    private final String field;
    private final EqualityOperator op;
    private final String value;

    public Attribute(String field, EqualityOperator op, String value) {
        this.op = op;
        this.field = mapField(field);
        this.value = mapValue(value);
    }

    /**
     * maps the passed field to one of the valid field names for the backend server:
     * <dl>
     *     <dt>dateCreated</dt>
     *     <dd>
     *         <ul>
     *             <li>dateCreated</li>
     *             <li>createDate</li>
     *             <li>date</li>
     *         </ul>
     *     </dd>
     *     <dt>fileSize</dt>
     *     <dd>
     *         <ul>
     *             <li>fileSize</li>
     *             <li>size</li>
     *             <li>length</li>
     *         </ul>
     *     </dd>
     *     <dt>fileType</dt>
     *     <dd>
     *         <ul>
     *             <li>fileType</li>
     *             <li>type</li>
     *         </ul>
     *     </dd>
     * </dl>
     *
     * @param field
     * @return the mapped field name listed above. If no mapping can be matched, the field itself is returned
     */
    private String mapField(String field) {
        return switch (field.toLowerCase()) {
            case "datecreated", "createdate", "date" -> "dateCreated";
            case "filesize", "size", "length" -> "fileSize";
            case "filetype", "type" -> "fileType";
            default -> field;
        };
    }

    private String mapValue(String value) {
        if ("fileSize".equals(this.field)) {
            return handleFileSizeByteAlias(value);
        }
        return value.toLowerCase();
    }

    private String handleFileSizeByteAlias(String value) {
        var matcher = BYTE_MULT_PATTERN.matcher(value);
        if (!matcher.find()) {
            return value;
        }
        // matcher found something, we are guaranteed to have a number and a mult (even if number is an empty string)
        var number = matcher.group("number");
        var mult = matcher.group("mult");
        var parsedNum = "".equals(number) ? 1 : Integer.parseInt(number);
        var byteMult = switch (mult.toLowerCase()) {
            case "kb" -> 1000;
            case "kib" -> 1024;
            case "mb" -> 1_000_000;
            case "mib" -> 1_048_576;
            case "gb" -> 1_000_000_000;
            case "gib" -> 1_073_741_824;
            case "tb" -> 1_000_000_000_000L;
            case "tib" -> 1_099_511_627_776L;
            case "pb" -> 1_000_000_000_000_000L;
            case "pib" -> 1_125_899_906_842_624L;
            default -> 1;
        };
        return String.valueOf(parsedNum * byteMult);
    }

    @Override
    public String toString() {
        return field + "." + op.toString() + ";" + value;
    }
}
