# -*- coding: utf-8 -*-
"""colorIdentifier1.ipynb

Automatically generated by Colaboratory.

Original file is located at
    https://colab.research.google.com/drive/1lhVVRaBTnFgSZAooLTByT0Tft7w62SH8
"""

import webcolors
from colorthief import ColorThief
from PIL import Image

CSS3_NAMES_TO_HEX = {
    'aliceblue': '#f0f8ff', 'antiquewhite': '#faebd7', 'aqua': '#00ffff', 'aquamarine': '#7fffd4',
    'azure': '#f0ffff', 'beige': '#f5f5dc', 'bisque': '#ffe4c4', 'black': '#000000',
    'blanchedalmond': '#ffebcd', 'blue': '#0000ff', 'blueviolet': '#8a2be2', 'brown': '#a52a2a',
    'burlywood': '#deb887', 'cadetblue': '#5f9ea0', 'chartreuse': '#7fff00', 'chocolate': '#d2691e',
    'coral': '#ff7f50', 'cornflowerblue': '#6495ed', 'cornsilk': '#fff8dc', 'crimson': '#dc143c',
    'cyan': '#00ffff', 'darkblue': '#00008b', 'darkcyan': '#008b8b', 'darkgoldenrod': '#b8860b',
    'darkgray': '#a9a9a9', 'darkgreen': '#006400', 'darkkhaki': '#bdb76b', 'darkmagenta': '#8b008b',
    'darkolivegreen': '#556b2f', 'darkorange': '#ff8c00', 'darkorchid': '#9932cc', 'darkred': '#8b0000',
    'darksalmon': '#e9967a', 'darkseagreen': '#8fbc8f', 'darkslateblue': '#483d8b', 'darkslategray': '#2f4f4f',
    'darkturquoise': '#00ced1', 'darkviolet': '#9400d3', 'deeppink': '#ff1493', 'deepskyblue': '#00bfff',
    'dimgray': '#696969', 'dodgerblue': '#1e90ff', 'firebrick': '#b22222', 'floralwhite': '#fffaf0',
    'forestgreen': '#228b22', 'fuchsia': '#ff00ff', 'gainsboro': '#dcdcdc', 'ghostwhite': '#f8f8ff',
    'gold': '#ffd700', 'goldenrod': '#daa520', 'gray': '#808080', 'green': '#008000', 'greenyellow': '#adff2f',
    'honeydew': '#f0fff0', 'hotpink': '#ff69b4', 'indianred': '#cd5c5c', 'indigo': '#4b0082', 'ivory': '#fffff0',
    'khaki': '#f0e68c', 'lavender': '#e6e6fa', 'lavenderblush': '#fff0f5', 'lawngreen': '#7cfc00', 'lemonchiffon': '#fffacd',
    'lightblue': '#add8e6', 'lightcoral': '#f08080', 'lightcyan': '#e0ffff', 'lightgoldenrodyellow': '#fafad2',
    'lightgreen': '#90ee90', 'lightgrey': '#d3d3d3', 'lightpink': '#ffb6c1', 'lightsalmon': '#ffa07a',
    'lightseagreen': '#20b2aa', 'lightskyblue': '#87cefa', 'lightslategray': '#778899', 'lightsteelblue': '#b0c4de',
    'lightyellow': '#ffffe0', 'lime': '#00ff00', 'limegreen': '#32cd32', 'linen': '#faf0e6', 'magenta': '#ff00ff',
    'maroon': '#800000', 'mediumaquamarine': '#66cdaa', 'mediumblue': '#0000cd', 'mediumorchid': '#ba55d3',
    'mediumpurple': '#9370db', 'mediumseagreen': '#3cb371', 'mediumslateblue': '#7b68ee', 'mediumspringgreen': '#00fa9a',
    'mediumturquoise': '#48d1cc', 'mediumvioletred': '#c71585', 'midnightblue': '#191970', 'mintcream': '#f5fffa',
    'mistyrose': '#ffe4e1', 'moccasin': '#ffe4b5', 'navajowhite': '#ffdead', 'navy': '#000080', 'oldlace': '#fdf5e6',
    'olive': '#808000', 'olivedrab': '#6b8e23', 'orange': '#ffa500', 'orangered': '#ff4500', 'orchid': '#da70d6',
    'palegoldenrod': '#eee8aa', 'palegreen': '#98fb98', 'paleturquoise': '#afeeee', 'palevioletred': '#db7093',
    'papayawhip': '#ffefd5', 'peachpuff': '#ffdab9', 'peru': '#cd853f', 'pink': '#ffc0cb', 'plum': '#dda0dd',
    'powderblue': '#b0e0e6', 'purple': '#800080', 'rebeccapurple': '#663399', 'red': '#ff0000', 'rosybrown': '#bc8f8f',
    'royalblue': '#4169e1', 'saddlebrown': '#8b4513', 'salmon': '#fa8072', 'sandybrown': '#f4a460', 'seagreen': '#2e8b57',
    'seashell': '#fff5ee', 'sienna': '#a0522d', 'silver': '#c0c0c0', 'skyblue': '#87ceeb', 'slateblue': '#6a5acd',
    'slategray': '#708090', 'snow': '#fffafa', 'springgreen': '#00ff7f', 'steelblue': '#4682b4', 'tan': '#d2b48c',
    'teal': '#008080', 'thistle': '#d8bfd8', 'tomato': '#ff6347', 'turquoise': '#40e0d0', 'violet': '#ee82ee',
    'wheat': '#f5deb3', 'white': '#ffffff', 'whitesmoke': '#f5f5f5', 'yellow': '#ffff00', 'yellowgreen': '#9acd32'
}

def get_colors(image_file, numcolors=5, resize=150):
    # Open the image and resize it
    img = Image.open(image_file)
    img.thumbnail((resize, resize))

    # Get the dominant color and color palette
    ct = ColorThief(image_file)
    palette = ct.get_palette(color_count=numcolors)

    hex_formatted_palette = []
    closest_family_names = []
    for color in palette:
        try:
            color_name = webcolors.rgb_to_name(color)
        except ValueError:
            color_name = get_closest_color_name(color)

        formatted_color = '#{0:02x}{1:02x}{2:02x}'.format(*color)
        hex_formatted_palette.append(formatted_color)

        parent_color = identify_parent_color(color_name)
        closest_family_names.append(parent_color)

        closest_family_names = duplicatecolors(closest_family_names)

    return closest_family_names

def get_closest_color_name(rgb):
    min_diff = None
    closest_color_name = None

    for color_name, color_hex in CSS3_NAMES_TO_HEX.items():
        r, g, b = webcolors.hex_to_rgb(color_hex)
        diff = (r - rgb[0]) ** 2 + (g - rgb[1]) ** 2 + (b - rgb[2]) ** 2

        if min_diff is None or diff < min_diff:
            min_diff = diff
            closest_color_name = color_name

    return closest_color_name

def identify_parent_color(cname):
    distinctive_colors = {
        'white': ['white', 'snow', 'seashell', 'antiquewhite', 'floralwhite', 'ivory', 'honeydew',
                  'mintcream', 'azure', 'aliceblue', 'ghostwhite', 'lavenderblush'],
        'gray': ['dimgray', 'dimgrey', 'gray', 'grey', 'darkgray', 'darkgrey', 'silver',
                 'lightgray', 'lightgrey', 'gainsboro', 'whitesmoke'],
        'red': ['orangered', 'coral', 'darksalmon', 'tomato', 'salmon', 'mistyrose', 'red',
                'darkred', 'maroon', 'firebrick', 'indianred', 'lightcoral'],
        'brown': ['linen', 'peru', 'peachpuff', 'sandybrown', 'saddlebrown', 'chocolate', 'sienna',
                  'brown', 'tan', 'burlywood', 'bisque'],
        'orange': ['moccasin', 'papayawhip', 'blanchedalmond', 'navajowhite', 'darkorange'],
        'yellow': ['lightgoldenrodyellow', 'lightyellow', 'beige', 'ivory', 'darkkhaki',
                   'palegoldenrod', 'khaki', 'lemonchiffon', 'gold', 'cornsilk', 'goldenrod',
                   'darkgoldenrod', 'floralwhite', 'oldlace'],
        'green': ['aquamarine', 'mediumaquamarine', 'mediumspringgreen', 'mintcream', 'springgreen',
                  'mediumseagreen', 'lime', 'green', 'darkgreen', 'limegreen', 'forestgreen',
                  'lightgreen', 'palegreen', 'darkseagreen', 'honeydew', 'lawngreen', 'chartreuse',
                  'greenyellow', 'darkolivegreen', 'yellowgreen', 'olivedrab'],
        'blue': ['blue', 'mediumblue', 'darkblue', 'navy', 'midnightblue', 'lavender', 'ghostwhite',
                 'royalblue', 'cornflowerblue', 'lightsteelblue', 'slategrey', 'slategray',
                 'lightslategray', 'lightslategrey', 'dodgerblue', 'aliceblue', 'steelblue',
                 'lightskyblue', 'skyblue', 'deepskyblue', 'lightblue', 'powderblue', 'cadetblue',
                 'darkturquoise', 'cyan', 'aqua', 'darkcyan', 'teal', 'darkslategray',
                 'darkslategrey', 'paleturquoise', 'lightcyan', 'azure', 'mediumturquoise',
                 'lightseagreen', 'turquoise'],
        'violet': ['darkmagenta', 'purple', 'violet', 'plum', 'thistle', 'mediumorchid',
                   'darkviolet', 'darkorchid', 'indigo', 'blueviolet', 'rebeccapurple',
                   'mediumpurple', 'mediumslateblue', 'darkslateblue', 'slateblue'],
        'pink': ['fuchsia', 'magenta', 'orchid', 'mediumvioletred', 'deeppink', 'hotpink',
                 'lavenderblush', 'palevioletred', 'crimson', 'pink', 'lightpink'],
        'black': ['black']
    }

    cname = cname.lower()

    for parent_color, color_list in distinctive_colors.items():
        if cname in color_list:
            return parent_color

    return "Undefined"

def duplicatecolors(parent_colors):
    no_dupe_colors = []
    for color in parent_colors:
        if color not in no_dupe_colors:
            no_dupe_colors.append(color)
    return no_dupe_colors
