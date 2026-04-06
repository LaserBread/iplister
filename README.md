# IP Address List
This is a simple Android app that allows you to store IP addresses for all your hosts. Good for 
managing a network or homelab. It features storing IPv4 and IPv6 addresses, hostnames, and MAC addresses.

It uses a remote database to upload its files to, the source code of which is available 
[here](https://github.com/LaserBread/ip-note-database).

> [!NOTE]
> This app was created for my CS 492 final. As such, it's far from a production-worthy build. I may
> revisit this app and flesh it out, but given how [Google is putting Android into a "papers, 
> please" era](https://keepandroidopen.org/), I may cancel this plan.

## Setup
Use the companion Python library to set up the database. Use your LAN or a VPN to expose it on your
network. Download and install the app.

## Acknowledgements
This app uses [Sean Foley's IPAddress library](https://github.com/seancfoley/IPAddress).

Several RegExps were found on StackOverflow:
- [IPv4 Address validation from Danail Gabenski](https://stackoverflow.com/a/36760050)
- [MAC Address validation from netcoder](https://stackoverflow.com/a/4260512)