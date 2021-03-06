{
  "name": "wordcloud",
  "width": 400,
  "height": 400,
  "padding": {"top":0, "bottom":0, "left":0, "right":0},
  "data": [
    {
      "name": "table",
      "values": [
        {"text": "poem", "value": 80},
        {"text": "peom", "value": 44},
        {"text": "moep", "value": 40},
        {"text": "meop", "value": 36},
        {"text": "emop", "value": 32},
        {"text": "epom", "value": 28},
        {"text": "opem", "value": 24},
        {"text": "omep", "value": 20},
        {"text": "mope", "value": 56},
        {"text": "mepo", "value": 12},
        {"text": "pemo", "value": 10},
        {"text": "pome", "value": 10}
      ],
      "transform": [
        {
          "type": "wordcloud",
          "text": "data.text",
          "font": "Helvetica Neue",
          "fontSize": "data.value",
          "rotate": {"random": [-60,-30,0,30,60]}
        }
      ]
    }
  ],
  "marks": [
    {
      "type": "text",
      "from": {"data": "table"},
      "properties": {
        "enter": {
          "x": {"field": "x"},
          "y": {"field": "y"},
          "angle": {"field": "angle"},
          "align": {"value": "center"},
          "baseline": {"value": "alphabetic"},
          "font": {"field": "font"},
          "fontSize": {"field": "fontSize"},
          "text": {"field": "data.text"}
        },
        "update": {
          "fill": {"value": "steelblue"}
        },
        "hover": {
          "fill": {"value": "#f00"}
        }
      }
    }
  ]
}
