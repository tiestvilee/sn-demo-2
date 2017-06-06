package com.springernature.kachtml

fun attr(name: String) = KAttribute(name, "")

infix fun String.attr(value: String) = KAttribute(this, value)

fun id(id: String) = Id(id)
fun cl(className: String) = Class(className)

fun doctype(vararg params: Any): Doctype = Doctype(*params)

fun a(vararg params: Any): A = A(*params)

fun abbr(vararg params: Any): Abbr = Abbr(*params)

fun address(vararg params: Any): Address = Address(*params)

fun area(vararg params: Any): Area = Area(*params)

fun article(vararg params: Any): Article = Article(*params)

fun aside(vararg params: Any): Aside = Aside(*params)

fun audio(vararg params: Any): Audio = Audio(*params)

fun b(vararg params: Any): B = B(*params)

fun base(vararg params: Any): Base = Base(*params)

fun bdi(vararg params: Any): Bdi = Bdi(*params)

fun bdo(vararg params: Any): Bdo = Bdo(*params)

fun blockquote(vararg params: Any): Blockquote = Blockquote(*params)

fun body(vararg params: Any): Body = Body(*params)

fun br(vararg params: Any): Br = Br(*params)

fun button(vararg params: Any): Button = Button(*params)

fun canvas(vararg params: Any): Canvas = Canvas(*params)

fun caption(vararg params: Any): Caption = Caption(*params)

fun cite(vararg params: Any): Cite = Cite(*params)

fun code(vararg params: Any): Code = Code(*params)

fun col(vararg params: Any): Col = Col(*params)

fun colgroup(vararg params: Any): Colgroup = Colgroup(*params)

fun command(vararg params: Any): Command = Command(*params)

fun datalist(vararg params: Any): Datalist = Datalist(*params)

fun dd(vararg params: Any): Dd = Dd(*params)

fun del(vararg params: Any): Del = Del(*params)

fun details(vararg params: Any): Details = Details(*params)

fun dfn(vararg params: Any): Dfn = Dfn(*params)

fun div(vararg params: Any): Div = Div(*params)

fun dl(vararg params: Any): Dl = Dl(*params)

fun dt(vararg params: Any): Dt = Dt(*params)

fun em(vararg params: Any): Em = Em(*params)

fun embed(vararg params: Any): Embed = Embed(*params)

fun fieldset(vararg params: Any): Fieldset = Fieldset(*params)

fun figcaption(vararg params: Any): Figcaption = Figcaption(*params)

fun figure(vararg params: Any): Figure = Figure(*params)

fun footer(vararg params: Any): Footer = Footer(*params)

fun form(vararg params: Any): Form = Form(*params)

fun h1(vararg params: Any): H1 = H1(*params)

fun h2(vararg params: Any): H2 = H2(*params)

fun h3(vararg params: Any): H3 = H3(*params)

fun h4(vararg params: Any): H4 = H4(*params)

fun h5(vararg params: Any): H5 = H5(*params)

fun h6(vararg params: Any): H6 = H6(*params)

fun head(vararg params: Any): Head = Head(*params)

fun header(vararg params: Any): Header = Header(*params)

fun hgroup(vararg params: Any): Hgroup = Hgroup(*params)

fun hr(vararg params: Any): Hr = Hr(*params)

fun html(vararg params: Any): Html = Html(*params)

fun i(vararg params: Any): I = I(*params)

fun iframe(vararg params: Any): Iframe = Iframe(*params)

fun img(vararg params: Any): Img = Img(*params)

fun input(vararg params: Any): Input = Input(*params)

fun ins(vararg params: Any): Ins = Ins(*params)

fun kbd(vararg params: Any): Kbd = Kbd(*params)

fun keygen(vararg params: Any): Keygen = Keygen(*params)

fun label(vararg params: Any): Label = Label(*params)

fun legend(vararg params: Any): Legend = Legend(*params)

fun li(vararg params: Any): Li = Li(*params)

fun link(vararg params: Any): Link = Link(*params)

fun map(vararg params: Any): HtmlMap = HtmlMap(*params)

fun mark(vararg params: Any): Mark = Mark(*params)

fun menu(vararg params: Any): Menu = Menu(*params)

fun meta(vararg params: Any): Meta = Meta(*params)

fun meter(vararg params: Any): Meter = Meter(*params)

fun nav(vararg params: Any): Nav = Nav(*params)

fun noscript(vararg params: Any): Noscript = Noscript(*params)

fun htmlObject(vararg params: Any): HtmlObject = HtmlObject(*params)

fun ol(vararg params: Any): Ol = Ol(*params)

fun optgroup(vararg params: Any): Optgroup = Optgroup(*params)

fun option(vararg params: Any): Option = Option(*params)

fun output(vararg params: Any): Output = Output(*params)

fun p(vararg params: Any): P = P(*params)

fun param(vararg params: Any): Param = Param(*params)

fun pre(vararg params: Any): Pre = Pre(*params)

fun progress(vararg params: Any): Progress = Progress(*params)

fun q(vararg params: Any): Q = Q(*params)

fun rp(vararg params: Any): Rp = Rp(*params)

fun rt(vararg params: Any): Rt = Rt(*params)

fun ruby(vararg params: Any): Ruby = Ruby(*params)

fun s(vararg params: Any): S = S(*params)

fun samp(vararg params: Any): Samp = Samp(*params)

fun script(vararg params: Any): Script = Script(*params)

fun section(vararg params: Any): Section = Section(*params)

fun select(vararg params: Any): Select = Select(*params)

fun small(vararg params: Any): Small = Small(*params)

fun source(vararg params: Any): Source = Source(*params)

fun span(vararg params: Any): Span = Span(*params)

fun strong(vararg params: Any): Strong = Strong(*params)

fun style(vararg params: Any): Style = Style(*params)

fun sub(vararg params: Any): Sub = Sub(*params)

fun summary(vararg params: Any): Summary = Summary(*params)

fun sup(vararg params: Any): Sup = Sup(*params)

fun table(vararg params: Any): Table = Table(*params)

fun tbody(vararg params: Any): Tbody = Tbody(*params)

fun td(vararg params: Any): Td = Td(*params)

fun textarea(vararg params: Any): Textarea = Textarea(*params)

fun tfoot(vararg params: Any): Tfoot = Tfoot(*params)

fun th(vararg params: Any): Th = Th(*params)

fun thead(vararg params: Any): Thead = Thead(*params)

fun time(vararg params: Any): Time = Time(*params)

fun title(vararg params: Any): Title = Title(*params)

fun tr(vararg params: Any): Tr = Tr(*params)

fun track(vararg params: Any): Track = Track(*params)

fun u(vararg params: Any): U = U(*params)

fun ul(vararg params: Any): Ul = Ul(*params)

fun `var`(vararg params: Any): Var = Var(*params)

fun video(vararg params: Any): Video = Video(*params)

fun wbr(vararg params: Any): Wbr = Wbr(*params)

