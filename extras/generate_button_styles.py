#! /usr/bin/python3
# list of our theme color names. Each sub array is the main button color and the color of the text
types = [
    ['primary', 'text'],
    ['secondary', 'background'],
    ['success', 'text'],
    ['warning', 'background'],
    ['error', 'text']
]


def basic(type):
    return f"""
.btn-{type[0]} {{
    -fx-background-color: -{type[0]};
    -fx-text-fill: -{type[1]}
}}

.btn-{type[0]}-outline {{
    -fx-border-style: dashed;
    -fx-border-color: -{type[0]};
    -fx-background-color: none;
    -fx-text-fill: -text;
}}"""


def hover(type):
    return f"""
.btn-{type[0]}:hover {{
    -fx-background-color: -{type[0]}-1;
}}

.btn-{type[0]}-outline:hover {{
    -fx-border-style: none;
    -fx-background-color: -{type[0]}-1;
}}"""


def clicked(type):
    return f"""
.btn-{type[0]}:armed {{
    -fx-background-color: -{type[0]}-2;
}}

.btn-{type[0]}-outline:armed {{
    -fx-border-style: none;
    -fx-background-color: -{type[0]}-2;
}}"""


file = open('../src/main/resources/ui/buttons.css', 'w')
# language=css
file.write("""
@import "theme.css";

.btn {
    -fx-cursor: hand;
}
""")

for type in types:
    buttonCss = basic(type) + '\n' + hover(type) + '\n' + clicked(type) + '\n'
    file.write(buttonCss)

file.close()
